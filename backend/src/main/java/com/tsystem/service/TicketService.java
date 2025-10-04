package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.TicketRepository;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.tsystem.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository tickets;
    private final ProjectRepository projects;
    private final UserRepository users;

    private Project mustOwnProject(UUID projectId, String username) {
        Project p = projects.findById(projectId).orElseThrow(NotFoundException::new);
        if (!p.getUser().getUsername().equals(username)) throw new ForbiddenException();
        return p;
    }

    private User me(String username) {
        return users.findByUsername(username).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Ticket create(UUID projectId, TicketCreateRequest req, String username) {
        Project p = mustOwnProject(projectId, username);
        User author = me(username);
        Ticket t = Ticket.builder()
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .priority(req.getPriority())
                .project(p)
                .user(author)
                .build();
        return tickets.save(t);
    }

    @Transactional(readOnly = true)
    public List<Ticket> list(UUID projectId, String username) {
        Project p = mustOwnProject(projectId, username);
        return tickets.findByProjectIdOrderByCreatedAtDesc(p.getId());
    }

    @Transactional(readOnly = true)
    public Ticket get(UUID projectId, UUID ticketId, String username) {
        Project p = mustOwnProject(projectId, username);
        return tickets.findByIdAndProjectId(ticketId, p.getId()).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Ticket update(UUID projectId, UUID ticketId, TicketUpdateRequest req, String username) {
        Ticket t = get(projectId, ticketId, username);
        t.setName(req.getName());
        t.setDescription(req.getDescription());
        t.setType(req.getType());
        t.setPriority(req.getPriority());
        t.setState(req.getState());
        return tickets.save(t);
    }

    @Transactional
    public void delete(UUID projectId, UUID ticketId, String username) {
        Ticket t = get(projectId, ticketId, username);
        tickets.delete(t);
    }
}