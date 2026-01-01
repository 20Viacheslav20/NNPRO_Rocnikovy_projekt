package com.tsystem.model.mapper;

import com.tsystem.model.TicketComment;
import com.tsystem.model.dto.request.TicketCommentRequest;
import com.tsystem.model.dto.response.TicketCommentResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TicketCommentMapper {
    private TicketCommentMapper(){}

    public static TicketCommentResponse toResponse(TicketComment t){
        return TicketCommentResponse.builder()
                .id(t.getId())
                .ticketId(t.getTicketId())
                .commentAuthorId(t.getAuthorId())
                .text(t.getText())
                .build();
    }

    public static List<TicketCommentResponse> toResponseList(List<TicketComment> comments) {
        return comments.stream()
                .map(TicketCommentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
