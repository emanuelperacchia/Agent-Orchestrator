package com.antigravity.orchestrator.repository;

import com.antigravity.orchestrator.core.Role;
import com.antigravity.orchestrator.core.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatusAndAssignedTo(String status, Role role);
}
