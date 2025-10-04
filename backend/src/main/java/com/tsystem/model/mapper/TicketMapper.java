package com.tsystem.model.mapper;


import com.tsystem.model.Ticket;
import com.tsystem.model.dto.response.TicketResponse;

public final class TicketMapper {
    private TicketMapper(){}

    public static TicketResponse toResponse(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .type(t.getType())
                .priority(t.getPriority())
                .state(t.getState())
                .createdAt(t.getCreatedAt())
                .build();
    }
}