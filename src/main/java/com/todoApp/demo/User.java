package com.todoApp.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Entity
@Table(name = "user")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;



    @Column( unique = true)
    private String   username;



    @Column(nullable = false)
    private String firstname;



    @Column(nullable = false)

    private String  lastname;



    @Column(nullable = false, unique = true)
    @Email
    private String email;



    @Column(nullable = false)

    private String     pw;



    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime created;


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPw() {
        return pw;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}




