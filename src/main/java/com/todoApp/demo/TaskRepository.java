package com.todoApp.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    //retruns all tasks related to one user that we gave his id as an argument returns it as task obj
    List<Task> findByUserId(int userId);

}