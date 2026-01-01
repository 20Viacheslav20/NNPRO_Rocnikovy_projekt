package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    @Transactional
    public User create(UserRequest req) {
        User user = User.builder()
                .email(req.getEmail())
                .username(req.getEmail()) // username = email
                .name(req.getName())
                .surname(req.getSurname())
                .role(SystemRole.valueOf(req.getRole()))
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User update(UUID id, UserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.setEmail(req.getEmail());
        user.setUsername(req.getEmail()); // keep consistent
        user.setName(req.getName());
        user.setSurname(req.getSurname());
        user.setRole(SystemRole.valueOf(req.getRole()));

        // Optional password change
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return userRepository.save(user);
    }

    // DELETE /users/{id}
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        userRepository.delete(user);
    }

    @Transactional
    public void blockUser(UUID userId) {
        int updated = userRepository.blockUser(userId);
        if (updated == 0) {
            throw new EntityNotFoundException("User not found");
        }
    }

    @Transactional
    public void unblockUser(UUID userId) {
        int updated = userRepository.unblockUser(userId);
        if (updated == 0) {
            throw new EntityNotFoundException("User not found");
        }
    }
}
