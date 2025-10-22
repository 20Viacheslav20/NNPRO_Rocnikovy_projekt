package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    public User updateAuthUser(UUID userId, User authUser) {
        User existingUser = userRepository.findById(userId).orElseThrow();
        existingUser.setUsername(authUser.getUsername());
        existingUser.setEmail(authUser.getEmail());
        existingUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        existingUser.setRole(authUser.getRole());
//        existingUser.setActive(authUser.isActive());
        return userRepository.save(existingUser);
    }

    public User deleteUser(UUID id) {

        User deleted = userRepository.findById(id).orElseThrow(()->new NotFoundException("User not found."));
        userRepository.deleteById(id);
        return deleted;
    }




}
