package com.tsystem.repository;


import com.tsystem.model.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @EntityGraph(attributePaths = {"author", "assignee"})
    List<Ticket> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    @EntityGraph(attributePaths = {"author", "assignee"})
    Optional<Ticket> findByIdAndProjectId(UUID id, UUID projectId);

    @EntityGraph(attributePaths = {"author", "assignee"})
    List<Ticket> findByAssigneeId(UUID assigneeId);

}
