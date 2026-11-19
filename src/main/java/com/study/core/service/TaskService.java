package com.study.core.service;

import com.study.core.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TaskService {
    org.springframework.data.domain.Page<Task> getPage(String status, String priority, int page, int size, String sort);

    List<Task> search(String keyword);

    List<Task> createdToday();

    Task create(String title, Task.Priority priority);

    Task create(Task task);

    List<Task> getAll();

    List<Task> getAll(String status, String priority);

    Optional<Task> findById(Long id);

    Optional<Task> update(Long id, Task task);

    Task updateStatus(Long id, String status);

    void status(Long id, Task.Status status);

    void delete(Long id);

    Map<Task.Status, Long> statistics();

    UUID getStats();
}
