package com.study.core.service;

import com.study.core.config.AppProperties;
import com.study.core.exception.TaskNotFoundException;
import com.study.core.model.Task;
import com.study.core.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    private final TaskRepository repository;
    private final AppProperties properties;
    private final ObjectProvider<TaskStatsService> stats;
    private final Clock clock;

    public TaskServiceImpl(TaskRepository repository, AppProperties properties, ObjectProvider<TaskStatsService> stats, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.stats = stats;
        this.clock = clock;
    }

    @PostConstruct
    void init() {
        System.out.println("TaskService инициализирован");
    }

    @PreDestroy
    void destroy() {
        System.out.println("TaskService завершает работу");
    }

    public Task create(String title, Task.Priority priority) {
        return create(new Task(null, title, "", priority, Task.Status.NEW, null));
    }

    public Task create(Task input) {
        if (repository.count() >= properties.maxTasks())
            throw new IllegalStateException("Достигнут лимит задач: " + properties.maxTasks());
        Task task = prepare(input, null, LocalDateTime.now(clock).truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        return repository.save(task);
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public List<Task> getAll(String status, String priority) {
        Task.Status selectedStatus = parseStatus(status);
        Task.Priority selectedPriority = parsePriority(priority);
        if (selectedStatus != null && selectedPriority != null)
            return repository.findByStatusAndPriority(selectedStatus, selectedPriority);
        if (selectedStatus != null) return repository.findByStatus(selectedStatus);
        if (selectedPriority != null) return repository.findByPriority(selectedPriority);
        return repository.findAll();
    }

    public Page<Task> getPage(String status, String priority, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("page должен быть >= 0, size — от 1 до 100");
        String[] fields = sort.split(",", -1);
        if (fields.length > 2 || !Set.of("id", "title", "description", "status", "priority", "createdAt").contains(fields[0]))
            throw new IllegalArgumentException("Некорректное поле сортировки");
        Sort.Direction direction;
        try {
            direction = fields.length == 1 ? Sort.Direction.ASC : Sort.Direction.fromString(fields[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Направление сортировки: asc или desc");
        }
        var pageable = PageRequest.of(page, size, Sort.by(direction, fields[0]));
        Task.Status selectedStatus = parseStatus(status);
        Task.Priority selectedPriority = parsePriority(priority);
        if (selectedStatus != null && selectedPriority != null)
            return repository.findByStatusAndPriority(selectedStatus, selectedPriority, pageable);
        if (selectedStatus != null) return repository.findByStatus(selectedStatus, pageable);
        if (selectedPriority != null) return repository.findByPriority(selectedPriority, pageable);
        return repository.findAll(pageable);
    }

    public List<Task> search(String keyword) {
        if (keyword == null || keyword.isBlank())
            throw new IllegalArgumentException("Параметр keyword не должен быть пустым");
        String escaped = keyword.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return repository.searchByKeyword(escaped);
    }

    public List<Task> createdToday() {
        LocalDate today = LocalDate.now(clock);
        return repository.findCreatedToday(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Task> update(Long id, Task input) {
        return repository.findById(id).map(current -> repository.save(prepare(input, id, current.getCreatedAt())));
    }

    public Task updateStatus(Long id, String value) {
        Task.Status status = parseStatus(value);
        if (status == null) throw new IllegalArgumentException("Поле status обязательно");
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.setStatus(status);
        return repository.save(task);
    }

    public void status(Long id, Task.Status status) {
        updateStatus(id, status == null ? null : status.name());
    }

    public void delete(Long id) {
        if (repository.findById(id).isEmpty()) throw new TaskNotFoundException(id);
        repository.deleteById(id);
    }

    public Map<Task.Status, Long> statistics() {
        Map<Task.Status, Long> result = new EnumMap<>(Task.Status.class);
        for (Task.Status value : Task.Status.values()) result.put(value, 0L);
        for (Object[] row : repository.countGroupedByStatus())
            result.put((Task.Status) row[0], ((Number) row[1]).longValue());
        return result;
    }

    public UUID getStats() {
        return stats.getObject().uuid();
    }

    private Task prepare(Task input, Long id, LocalDateTime createdAt) {
        if (input.getTitle() == null || input.getTitle().isBlank())
            throw new IllegalArgumentException("Название не может быть пустым");
        String title = input.getTitle().trim();
        String description = input.getDescription() == null ? "" : input.getDescription();
        if (title.length() > 255) throw new IllegalArgumentException("Название не должно превышать 255 символов");
        if (description.length() > 2000)
            throw new IllegalArgumentException("Описание не должно превышать 2000 символов");
        return new Task(id, title, description,
                input.getPriority() == null ? properties.defaultPriority() : input.getPriority(),
                input.getStatus() == null ? Task.Status.NEW : input.getStatus(), createdAt);
    }

    private Task.Status parseStatus(String value) {
        if (value == null) return null;
        try {
            return Task.Status.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Допустимые статусы: NEW, IN_PROGRESS, DONE");
        }
    }

    private Task.Priority parsePriority(String value) {
        if (value == null) return null;
        try {
            return Task.Priority.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Допустимые приоритеты: LOW, MEDIUM, HIGH");
        }
    }
}
