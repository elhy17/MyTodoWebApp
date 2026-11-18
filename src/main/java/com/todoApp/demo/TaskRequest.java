package com.todoApp.demo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Min(1) @Max(5) int priority,
        @Min(0) int categoryId,
        @Min(0) int expense,
        @Min(0) @Max(1) int done
) {
    public Task toTask() {
        Task task = new Task();
        applyTo(task);
        return task;
    }

    public void applyTo(Task task) {
        task.setTitle(title.trim());
        task.setPriority(priority);
        task.setCategoryId(categoryId);
        task.setExpense(expense);
        task.setDone(done);
    }
}
