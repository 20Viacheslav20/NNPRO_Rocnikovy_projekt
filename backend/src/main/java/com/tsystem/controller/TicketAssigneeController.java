package com.tsystem.controller;

import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.mapper.TicketMapper;
import com.tsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketAssigneeController {

    private final TicketService ticketService;

    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAuthority('ticket:read_assigned') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    public List<TicketResponse> getByAssignee(@PathVariable UUID userId) {

        return ticketService.findByAssignee(userId)
                .stream()
                .map(TicketMapper::toResponse)
                .toList();
    }
}
