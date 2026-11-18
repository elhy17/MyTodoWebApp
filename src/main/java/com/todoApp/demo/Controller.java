package com.todoApp.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Controller {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public Controller(UserRepository userRepository, TaskRepository taskRepository,
                      AuthenticationManager authenticationManager,
                      SecurityContextRepository securityContextRepository,
                      SessionAuthenticationStrategy sessionAuthenticationStrategy,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email()) || userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateAccountException();
        }
        User user = new User();
        user.setFirstname(request.firstname().trim());
        user.setLastname(request.lastname().trim());
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPw(passwordEncoder.encode(request.pw()));
        user.setCreated(LocalDateTime.now());
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userRepository.save(user)));
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new DuplicateAccountException();
        }
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim().toLowerCase(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw new BadCredentialsException("Invalid email or password");
        }
        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);
        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return UserResponse.from(userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow());
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(currentUser(authentication));
    }

    @GetMapping("/user/{id}")
    public UserResponse getUser(@PathVariable int id, Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getId() != id) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return UserResponse.from(user);
    }

    @GetMapping("/tasks")
    public List<TaskResponse> getTasks(Authentication authentication) {
        return taskRepository.findByUserId(currentUser(authentication).getId()).stream().map(TaskResponse::from).toList();
    }

    // Compatibility route: the supplied ID is checked against the authenticated account.
    @GetMapping("/tasks/{userId}")
    public List<TaskResponse> getUserTasks(@PathVariable int userId, Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getId() != userId) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return taskRepository.findByUserId(user.getId()).stream().map(TaskResponse::from).toList();
    }

    @PostMapping("/task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        Task task = request.toTask();
        task.setUserId(currentUser(authentication).getId());
        task.setCreated(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(taskRepository.save(task)));
    }

    @PutMapping("/task/{id}")
    public TaskResponse updateTask(@PathVariable int id, @Valid @RequestBody TaskRequest request, Authentication authentication) {
        Task task = ownedTask(id, authentication);
        request.applyTo(task);
        return TaskResponse.from(taskRepository.save(task));
    }

    @DeleteMapping("/task/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id, Authentication authentication) {
        taskRepository.delete(ownedTask(id, authentication));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Task ownedTask(int id, Authentication authentication) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        if (task.getUserId() != currentUser(authentication).getId()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return task;
    }
}
