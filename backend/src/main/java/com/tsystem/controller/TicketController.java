package com.tsystem.controller;


import com.tsystem.model.Ticket;
import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.mapper.TicketMapper;
import com.tsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tickets")
public class TicketController {

    private final TicketService tickets;

    // GET /projects/{projectId}/tickets
    @GetMapping
    public List<TicketResponse> list(@PathVariable UUID projectId,
                                     @AuthenticationPrincipal UserDetails principal) {
        return tickets.list(projectId, principal.getUsername())
                .stream().map(TicketMapper::toResponse).toList();
    }

    // POST /projects/{projectId}/tickets
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@PathVariable UUID projectId,
                                 @Valid @RequestBody TicketCreateRequest req,
                                 @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(tickets.create(projectId, req, principal.getUsername()));
    }

    // GET /projects/{projectId}/tickets/{ticketId}
    @GetMapping("/{ticketId}")
    public TicketResponse get(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                              @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(tickets.get(projectId, ticketId, principal.getUsername()));
    }

    // PUT /projects/{projectId}/tickets/{ticketId}
    @PutMapping("/{ticketId}")
    public TicketResponse update(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                                 @Valid @RequestBody TicketUpdateRequest req,
                                 @AuthenticationPrincipal UserDetails principal) {
        return TicketMapper.toResponse(tickets.update(projectId, ticketId, req, principal.getUsername()));
    }

    // DELETE /projects/{projectId}/tickets/{ticketId}
    @DeleteMapping("/{ticketId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID projectId, @PathVariable UUID ticketId,
                       @AuthenticationPrincipal UserDetails principal) {
        tickets.delete(projectId, ticketId, principal.getUsername());
    }
}