package com.todoApp.demo;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int userId;

    @Column
    private int categoryId;

    @Column
    private int priority;

    @Column
    private String title;

    @Column
    private int expense;

    @Column
    private int done;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime created;


    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public int getExpense() {
        return expense;
    }

    public int getDone() {
        return done;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    // SETTERS
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setExpense(int expense) {
        this.expense = expense;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}

