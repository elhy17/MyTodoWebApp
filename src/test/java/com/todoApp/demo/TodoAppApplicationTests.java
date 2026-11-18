package com.todoApp.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoAppApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository users;
    @Autowired TaskRepository tasks;

    @BeforeEach
    void clearDatabase() {
        tasks.deleteAll();
        users.deleteAll();
    }

    @Test
    void registrationReturnsSafeDtoAndRejectsDuplicateAccount() throws Exception {
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(registration("alice@example.com", "alice")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.pw").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(registration("alice@example.com", "alice2")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"\",\"lastname\":\"Smith\",\"username\":\"bad\",\"email\":\"not-email\",\"pw\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginEstablishesSessionAndUnauthenticatedRoutesAreRejected() throws Exception {
        register("alice@example.com", "alice");
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"incorrect-password\"}"))
                .andExpect(status().isUnauthorized());
        MvcResult login = mvc.perform(post("/api/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pw").doesNotExist())
                .andReturn();
        mvc.perform(get("/api/me").session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void usersCannotReadOrChangeAnotherUsersTasksAndOwnerComesFromSession() throws Exception {
        int aliceId = register("alice@example.com", "alice");
        int bobId = register("bob@example.com", "bob");
        org.springframework.mock.web.MockHttpSession aliceSession = login("alice@example.com");
        org.springframework.mock.web.MockHttpSession bobSession = login("bob@example.com");
        MvcResult created = mvc.perform(post("/api/task").session(aliceSession).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + bobId + ",\"title\":\"Alice task\",\"priority\":3,\"categoryId\":2,\"expense\":5,\"done\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andReturn();
        JsonNode task = objectMapper.readTree(created.getResponse().getContentAsString());
        int taskId = task.get("id").asInt();
        org.junit.jupiter.api.Assertions.assertEquals(aliceId, tasks.findById(taskId).orElseThrow().getUserId());

        mvc.perform(get("/api/tasks").session(bobSession)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/tasks/" + aliceId).session(bobSession)).andExpect(status().isForbidden());
        mvc.perform(put("/api/task/" + taskId).session(bobSession).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Stolen\",\"priority\":1,\"categoryId\":0,\"expense\":0,\"done\":0}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/task/" + taskId).session(bobSession).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(get("/api/user/" + bobId).session(aliceSession)).andExpect(status().isForbidden());
    }

    @Test
    void taskCrudValidatesFieldsAndScopesOperationsToOwner() throws Exception {
        register("alice@example.com", "alice");
        org.springframework.mock.web.MockHttpSession session = login("alice@example.com");
        mvc.perform(post("/api/task").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \s\",\"priority\":9,\"categoryId\":0,\"expense\":0,\"done\":0}"))
                .andExpect(status().isBadRequest());
        MvcResult created = mvc.perform(post("/api/task").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"First\",\"priority\":3,\"categoryId\":2,\"expense\":5,\"done\":0}"))
                .andExpect(status().isCreated()).andReturn();
        int taskId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asInt();
        mvc.perform(get("/api/tasks").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("First"));
        mvc.perform(put("/api/task/" + taskId).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":9999,\"title\":\"Updated\",\"priority\":5,\"categoryId\":3,\"expense\":10,\"done\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated"));
        mvc.perform(delete("/api/task/" + taskId).session(session).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    private int register(String email, String username) throws Exception {
        MvcResult result = mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registration(email, username)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }

    private org.springframework.mock.web.MockHttpSession login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isOk()).andReturn();
        return (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
    }

    private String registration(String email, String username) throws Exception {
        return objectMapper.writeValueAsString(new RegisterRequest("Alice", "Smith", username, email, "correct-horse-battery"));
    }
}
