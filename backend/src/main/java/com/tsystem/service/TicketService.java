package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.TicketRepository;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.tsystem.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Ticket create(UUID projectId, TicketCreateRequest req, String username) {

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User author = getUserByUsername(username);

        User assignee = null;

        if (req.getAssigneeId() != null) {
            assignee = userRepository.findById(req.getAssigneeId())
                    .orElse(null);
        }

        Ticket t = Ticket.builder()
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .priority(req.getPriority())
                .project(p)
                .author(author)
                .assignee(assignee)
                .build();

        return ticketRepository.save(t);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByAssignee(UUID assigneeId) {
        // Validate user first
        userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ticketRepository.findByAssigneeId(assigneeId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllByProjectId(UUID projectId) {

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return ticketRepository.findByProjectIdOrderByCreatedAtDesc(p.getId());
    }

    @Transactional(readOnly = true)
    public Ticket get(UUID projectId, UUID ticketId, String username) {
        Ticket t = ticketRepository.findByIdAndProjectId(ticketId, projectId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        User current = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // ADMIN -> always allowed
        if (current.getRole() == SystemRole.ADMIN) return t;

        // PROJECT_MANAGER -> always allowed
        if (current.getRole() == SystemRole.PROJECT_MANAGER) return t;

        // USER -> only if assigned
        if (current.getRole() == SystemRole.USER) {
            if (t.getAssignee() == null || !t.getAssignee().getId().equals(current.getId())) {
                throw new AccessDeniedException("You cannot view this ticket");
            }
            return t;
        }

        throw new AccessDeniedException("Access denied");
    }

    @Transactional
    public Ticket update(UUID projectId, UUID ticketId, TicketUpdateRequest req) {
        Ticket t = getTicket(projectId, ticketId);
        t.setName(req.getName());
        t.setDescription(req.getDescription());
        t.setType(req.getType());
        t.setPriority(req.getPriority());
        t.setState(req.getState());
        return ticketRepository.save(t);
    }

    @Transactional
    public void delete(UUID projectId, UUID ticketId) {
        Ticket t = getTicket(projectId, ticketId);
        ticketRepository.delete(t);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
    }

    private Ticket getTicket(UUID projectId, UUID ticketId){
        return ticketRepository.findByIdAndProjectId(ticketId, projectId).orElseThrow(NotFoundException::new);
    }
}