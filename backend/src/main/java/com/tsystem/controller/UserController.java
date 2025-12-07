package com.tsystem.controller;

import com.tsystem.model.dto.response.UserShortResponse;
import com.tsystem.model.mapper.ProjectMapper;
import com.tsystem.model.mapper.UserMapper;
import com.tsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserShortResponse> getAllUsers() {
        return userService.findAll()
                .stream().map(UserMapper::toResponse).toList();
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
