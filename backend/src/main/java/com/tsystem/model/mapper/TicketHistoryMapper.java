package com.tsystem.model.mapper;

import com.tsystem.model.TicketHistory;
import com.tsystem.model.dto.response.TicketHistoryResponse;

import java.util.List;

public class TicketHistoryMapper {

    public static TicketHistoryResponse toResponse(TicketHistory h) {
        return TicketHistoryResponse.builder()
                .id(h.getId())
                .authorId(h.getAuthorId())
                .action(h.getAction())
                .field(h.getField())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .createdAt(h.getCreatedAt())
                .build();
    }

    public static List<TicketHistoryResponse> toResponseList(List<TicketHistory> list) {
        return list.stream()
                .map(TicketHistoryMapper::toResponse)
                .toList();
    }
}

