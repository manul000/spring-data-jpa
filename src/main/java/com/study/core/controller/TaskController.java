package com.study.core.controller;

import com.study.core.exception.TaskNotFoundException;
import com.study.core.model.Task;
import com.study.core.service.TaskService;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public org.springframework.data.domain.Page<Task> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return taskService.getPage(status, priority, page, size, sort);
    }

    @GetMapping("/search")
    public List<Task> search(@RequestParam String keyword) {
        return taskService.search(keyword);
    }

    @GetMapping("/today")
    public List<Task> createdToday() {
        return taskService.createdToday();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id)));
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        Task created = taskService.create(task);
        return ResponseEntity.created(URI.create("/tasks/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.update(id, task).orElseThrow(() -> new TaskNotFoundException(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(taskService.updateStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<Task.Status, Long> statistics() {
        return taskService.statistics();
    }
}
