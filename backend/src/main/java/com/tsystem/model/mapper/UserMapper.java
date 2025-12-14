package com.tsystem.model.mapper;

import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.dto.response.UserResponse;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;

public class UserMapper {
    private UserMapper(){}

    public static UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .surname(u.getSurname())
                .username(u.getUsername())
                .role(u.getRole().toString())
                .build();
    }

    public static void update(User u, UserRequest req) {
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        u.setSurname(req.getSurname());
        u.setRole(SystemRole.valueOf(req.getRole()));
    }
}
