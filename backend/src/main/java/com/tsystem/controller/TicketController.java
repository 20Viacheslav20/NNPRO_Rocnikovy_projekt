package com.tsystem.controller;


import com.tsystem.model.dto.request.TicketCommentRequest;
import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.model.dto.response.TicketCommentResponse;
import com.tsystem.model.dto.response.TicketHistoryResponse;
import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.mapper.TicketCommentMapper;
import com.tsystem.model.mapper.TicketHistoryMapper;
import com.tsystem.model.mapper.TicketMapper;
import com.tsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tickets")
public class TicketController {

    private final TicketService ticketService;

    // GET /projects/{projectId}/tickets
    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public List<TicketResponse> list(@PathVariable UUID projectId) {
        return ticketService.getAllByProjectId(projectId)
                .stream().map(TicketMapper::toResponse).toList();
    }

    // POST /projects/{projectId}/tickets
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public TicketResponse create(@PathVariable UUID projectId,
                                 @Valid @RequestBody TicketCreateRequest req,
                                 @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(ticketService.create(projectId, req, principal.getUsername()));
    }

    // GET /projects/{projectId}/tickets/{ticketId}
    @GetMapping("/{ticketId}")
    public TicketResponse get(@PathVariable UUID projectId, @PathVariable UUID ticketId) {
        return TicketMapper.toResponse(ticketService.get(projectId, ticketId));
    }

    // PUT /projects/{projectId}/tickets/{ticketId}
    @PutMapping("/{ticketId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasAuthority('ticket:update_assigned') ")
    public TicketResponse update(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                                 @Valid @RequestBody TicketUpdateRequest req,
                                 @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(ticketService.update(projectId, ticketId, req, principal.getUsername()));
    }

    // DELETE /projects/{projectId}/tickets/{ticketId}
    @DeleteMapping("/{ticketId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public void delete(@PathVariable UUID projectId, @PathVariable UUID ticketId, @AuthenticationPrincipal UserDetails principal) {
        ticketService.delete(projectId, ticketId, principal.getUsername());
    }



    @GetMapping("/{ticketId}/comments")
    public List<TicketCommentResponse> listComments(@PathVariable UUID ticketId) {
        return TicketCommentMapper.toResponseList(ticketService.getComments(ticketId));
    }

    @PostMapping("/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentResponse addComment(@PathVariable UUID ticketId,
                                            @Valid @RequestBody TicketCommentRequest req,
                                            @AuthenticationPrincipal UserDetails principal) {
        return TicketCommentMapper.toResponse(
                ticketService.addComment(ticketId, req, principal.getUsername())
        );
    }

    @PutMapping("/{ticketId}/comments/{commentId}")
    public TicketCommentResponse updateComment(@PathVariable UUID commentId,
                                               @Valid @RequestBody TicketCommentRequest req) {
        return TicketCommentMapper.toResponse(ticketService.updateComment(commentId, req));
    }

    @DeleteMapping("/{ticketId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID commentId) {
        ticketService.deleteComment(commentId);
    }

    @GetMapping("/{ticketId}/history")
    public List<TicketHistoryResponse> getHistory(@PathVariable UUID ticketId) {
        return TicketHistoryMapper.toResponseList(ticketService.getHistory(ticketId));
    }
}