package com.tsystem.model.mapper;

import com.tsystem.model.dto.response.UserShortResponse;
import com.tsystem.model.user.User;

public class UserMapper {
    private UserMapper(){}

    public static UserShortResponse toResponse(User u) {
        return UserShortResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .surname(u.getSurname())
                .username(u.getUsername())
                .build();
    }
}
