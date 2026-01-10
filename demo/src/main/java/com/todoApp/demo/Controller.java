package com.todoApp.demo;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // user endpoints

    @PostMapping("/register")
    public User register( @Valid @RequestBody User user) {
        user.setPw(passwordEncoder.encode(user.getPw()));
        user.setCreated(LocalDateTime.now());
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPw())) {
            return user;
        }
        return null;
    }


    @GetMapping("/user/{id}")
    public User getUser(@PathVariable int id) {
        return userRepository.findById(id).orElse(null);
    }

    // TASK ENDPOINTS

    @PostMapping("/task")
    public Task createTask(@RequestBody Task task) {
        task.setCreated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @GetMapping("/tasks/{userId}")
    public List<Task> getUserTasks(@PathVariable int userId) {
        return taskRepository.findByUserId(userId);
    }

    @PutMapping("/task/{id}")
    public Task updateTask(@PathVariable int id, @RequestBody Task task) {
        task.setId(id);
        return taskRepository.save(task);
    }

    @DeleteMapping("/task/{id}")
    public void deleteTask(@PathVariable int id) {
        taskRepository.deleteById(id);
    }
}
