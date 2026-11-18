package com.todoApp.demo;

import java.time.LocalDateTime;

public record UserResponse(int id, String username, String firstname, String lastname, String email,
                           LocalDateTime created) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFirstname(), user.getLastname(),
                user.getEmail(), user.getCreated());
    }
}
