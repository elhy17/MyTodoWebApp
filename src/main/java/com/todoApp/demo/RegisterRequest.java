
package com.todoApp.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// introduced this record to make sure that no user id comes with a request 'security reasons'
public record RegisterRequest(

        @NotBlank @Size(max = 80)
        String firstname,

        @NotBlank @Size(max = 80)
        String lastname,

        @NotBlank @Size(min = 3, max = 50)
        String username,

        @NotBlank @Size(max = 254)
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String pw

) {}
