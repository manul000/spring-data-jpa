package com.study.core.service;

import com.study.core.model.Task;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TaskStatsService {
    private final UUID uuid = UUID.randomUUID();

    public UUID uuid() {
        return uuid;
    }

    public Map<Task.Status, Long> countByStatus(List<Task> tasks) {
        Map<Task.Status, Long> result = new EnumMap<>(Task.Status.class);
        for (Task.Status status : Task.Status.values()) result.put(status, 0L);
        for (Task task : tasks) result.merge(task.getStatus(), 1L, Long::sum);
        return result;
    }
}
