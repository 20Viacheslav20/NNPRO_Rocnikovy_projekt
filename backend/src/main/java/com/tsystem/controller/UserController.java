package com.tsystem.controller;

import com.tsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
//@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {

    private final UserService userService;


//    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

//    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
