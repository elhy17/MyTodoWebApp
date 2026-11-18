package com.todoApp.demo;

import java.time.LocalDateTime;

public record TaskResponse(int id, String title, int priority, int categoryId, int expense, int done,
                          LocalDateTime created) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getPriority(), task.getCategoryId(),
                task.getExpense(), task.getDone(), task.getCreated());
    }
}
