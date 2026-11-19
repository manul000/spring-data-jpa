package com.study.core.repository;

import com.study.core.model.Task;
import com.study.core.model.Task.Status;
import com.study.core.model.Task.Priority;
import com.study.core.exception.TaskNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(Status status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByStatusAndPriority(Status status, Priority priority);

    List<Task> findByTitleContainingIgnoreCase(String keyword);

    long countByStatus(Status status);

    Page<Task> findByStatus(Status status, Pageable pageable);

    Page<Task> findByPriority(Priority priority, Pageable pageable);

    Page<Task> findByStatusAndPriority(Status status, Priority priority, Pageable pageable);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :kw, '%')) ESCAPE '!' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :kw, '%')) ESCAPE '!' ORDER BY t.id")
    List<Task> searchByKeyword(@Param("kw") String keyword);

    @Query("SELECT t FROM Task t WHERE t.createdAt >= :start AND t.createdAt < :end ORDER BY t.id")
    List<Task> findCreatedToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    default void delete(Long id) {
        deleteById(id);
    }

    default void updateStatus(Long id, Task.Status status) {
        Task task = findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.setStatus(status);
        save(task);
    }

}
