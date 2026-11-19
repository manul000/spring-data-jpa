package com.study.core.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Задача не найдена: " + id);
    }
}
