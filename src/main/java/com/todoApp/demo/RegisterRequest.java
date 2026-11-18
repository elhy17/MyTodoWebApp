
package com.todoApp.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// introduced this record to make sure that no user id comes with a request 'security reasons'
public record RegisterRequest(

        @NotBlank
        String firstname,

        @NotBlank
        String lastname,

        @NotBlank
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8)
        String pw

) {}
