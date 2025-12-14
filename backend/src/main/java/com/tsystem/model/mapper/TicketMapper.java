package com.tsystem.model.mapper;


import com.tsystem.model.Ticket;
import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.dto.response.UserShortResponse;
import com.tsystem.model.user.User;

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
                .owner(toUserResponse(t.getAuthor()))
                .assignee(t.getAssignee() != null ? toUserResponse(t.getAssignee()) : null)
                .projectId(t.getProject().getId())
                .build();
    }

    private static UserShortResponse toUserResponse(User u) {
        return UserShortResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .surname(u.getSurname())
                .build();
    }

}