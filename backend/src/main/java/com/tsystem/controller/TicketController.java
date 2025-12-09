package com.tsystem.controller;


import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.model.dto.response.TicketResponse;
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

    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAuthority('ticket:read_assigned') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public List<TicketResponse> getByAssignee(@PathVariable UUID userId) {

        return ticketService.findByAssignee(userId)
                .stream()
                .map(TicketMapper::toResponse)
                .toList();
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
    public TicketResponse get(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                              @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(ticketService.get(projectId, ticketId, principal.getUsername()));
    }

    // PUT /projects/{projectId}/tickets/{ticketId}
    @PutMapping("/{ticketId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasAuthority('ticket:update_assigned') ")
    public TicketResponse update(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                                 @Valid @RequestBody TicketUpdateRequest req) {
        return TicketMapper.toResponse(ticketService.update(projectId, ticketId, req));
    }

    // DELETE /projects/{projectId}/tickets/{ticketId}
    @DeleteMapping("/{ticketId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public void delete(@PathVariable UUID projectId, @PathVariable UUID ticketId) {
        ticketService.delete(projectId, ticketId);
    }
}