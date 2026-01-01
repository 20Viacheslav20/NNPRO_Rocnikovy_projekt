package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.TicketComment;
import com.tsystem.model.TicketHistory;
import com.tsystem.model.dto.request.TicketCommentRequest;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Transactional
    public Ticket create(UUID projectId, TicketCreateRequest req, String username) {

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User author = getUserByUsername(username);

        Ticket t = Ticket.builder()
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .priority(req.getPriority())
                .project(p)
                .author(author)
            .build();

        Ticket saved = ticketRepository.save(t);

        logHistory(
                saved.getId(),
                author.getId(),
                "CREATED",
                null,
                null,
                null
        );

        return saved;
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
    public Ticket get(UUID projectId, UUID ticketId) {
        return ticketRepository.findByIdAndProjectId(ticketId, projectId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    @Transactional
    public Ticket update(UUID projectId, UUID ticketId, TicketUpdateRequest req, String username) {

        Ticket t = getTicket(projectId, ticketId);
        User actor = getUserByUsername(username);

        if (!Objects.equals(t.getName(), req.getName())) {
            logHistory(ticketId, actor.getId(),
                    "UPDATED", "name", t.getName(), req.getName());
            t.setName(req.getName());
        }

        if (!Objects.equals(t.getDescription(), req.getDescription())) {
            logHistory(ticketId, actor.getId(),
                    "UPDATED", "description", t.getDescription(), req.getDescription());
            t.setDescription(req.getDescription());
        }

        if (!Objects.equals(t.getPriority(), req.getPriority())) {
            logHistory(ticketId, actor.getId(),
                    "UPDATED", "priority", t.getPriority(), req.getPriority());
            t.setPriority(req.getPriority());
        }

        if (!Objects.equals(t.getState(), req.getState())) {
            logHistory(ticketId, actor.getId(),
                    "UPDATED", "state", t.getState(), req.getState());
            t.setState(req.getState());
        }

        return ticketRepository.save(t);
    }


    @Transactional
    public void delete(UUID projectId, UUID ticketId, String username) {

        Ticket t = getTicket(projectId, ticketId);
        User actor = getUserByUsername(username);

        logHistory(
                ticketId,
                actor.getId(),
                "DELETED",
                null,
                null,
                null
        );

        ticketRepository.delete(t);
    }


    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
    }

    private Ticket getTicket(UUID projectId, UUID ticketId){
        return ticketRepository.findByIdAndProjectId(ticketId, projectId).orElseThrow(NotFoundException::new);
    }


    // --------- COMMENTS ---------

    @Transactional(readOnly = true)
    public List<TicketComment> getComments(UUID ticketId) {

        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public TicketComment addComment(UUID ticketId, TicketCommentRequest request, String username) {

        User author = getUserByUsername(username);

        TicketComment comment = TicketComment.builder()
                .ticketId(ticketId)
                .authorId(author.getId())
                .text(request.getText())
                .build();

        return ticketCommentRepository.save(comment);
    }

    @Transactional
    public TicketComment updateComment(UUID commentId,
                                       TicketCommentRequest request) {

        TicketComment comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(NotFoundException::new);

        comment.setText(request.getText());
        return ticketCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId) {

        TicketComment comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(NotFoundException::new);

        ticketCommentRepository.delete(comment);
    }

    private void logHistory(UUID ticketId,
                            UUID authorId,
                            String action,
                            String field,
                            Object oldValue,
                            Object newValue) {

        TicketHistory h = TicketHistory.builder()
                .ticketId(ticketId)
                .authorId(authorId)
                .action(action)
                .field(field)
                .oldValue(oldValue != null ? oldValue.toString() : null)
                .newValue(newValue != null ? newValue.toString() : null)
                .createdAt(OffsetDateTime.now())
                .build();

        ticketHistoryRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> getHistory(UUID ticketId) {
        return ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

}