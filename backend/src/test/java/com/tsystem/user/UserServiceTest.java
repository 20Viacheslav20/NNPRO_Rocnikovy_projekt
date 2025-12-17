package com.tsystem.user;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder().id(userId).email("test@example.com").username("test@example.com")
                .name("Test").surname("User").role(SystemRole.USER).password("hashedPassword").build();
    }

    @Test @DisplayName("findAll - returns all users")
    void findAll_ReturnsUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, User.builder().id(UUID.randomUUID()).build()));
        assertEquals(2, userService.findAll().size());
    }

    @Test @DisplayName("findAll - empty list")
    void findAll_Empty() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertTrue(userService.findAll().isEmpty());
    }

    @Test @DisplayName("findById - successfuly found")
    void findById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        assertEquals(testUser, userService.findById(userId));
    }

    @Test @DisplayName("findById - not found")
    void findById_NotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findById(UUID.randomUUID()));
    }

    @Test @DisplayName("findByEmail - successfully found")
    void findByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        assertEquals(testUser, userService.findByEmail("test@example.com"));
    }

    @Test @DisplayName("findByEmail - not found")
    void findByEmail_NotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findByEmail("x@y.com"));
    }

    @Test @DisplayName("findByUsername - successfully found")
    void findByUsername_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
        assertEquals(testUser, userService.findByUsername("test@example.com"));
    }

    @Test @DisplayName("findByUsername - not found")
    void findByUsername_NotFound() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findByUsername("notfound"));
    }

    @Test @DisplayName("create - successful creation")
    void create_Success() {
        UserRequest req = UserRequest.builder().email("new@test.com").name("New").surname("User").password("pass").role("USER").build();
        when(passwordEncoder.encode("pass")).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.create(req);
        assertEquals("new@test.com", result.getEmail());
        assertEquals(SystemRole.USER, result.getRole());
    }

    @Test @DisplayName("create with ADMIN role")
    void create_AsAdmin() {
        UserRequest req = UserRequest.builder().email("admin@test.com").name("A").surname("B").password("p").role("ADMIN").build();
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(SystemRole.ADMIN, userService.create(req).getRole());
    }

    @Test @DisplayName("update without password change")
    void update_WithoutPassword() {
        UserRequest req = UserRequest.builder().email("upd@test.com").name("Upd").surname("N").role("ADMIN").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(userId, req);
        assertEquals("upd@test.com", result.getEmail());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test @DisplayName("update with new password")
    void update_WithPassword() {
        UserRequest req = UserRequest.builder().email("upd@test.com").name("U").surname("N").password("newPass").role("USER").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(userId, req);
        assertEquals("newHash", result.getPassword());
    }

    @Test @DisplayName("update - not found")
    void update_NotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        UserRequest req = UserRequest.builder().email("x@y.com").name("X").surname("Y").role("USER").build();
        assertThrows(NotFoundException.class, () -> userService.update(UUID.randomUUID(), req));
    }

    @Test @DisplayName("delete - successful removal")
    void delete_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        userService.delete(userId);
        verify(userRepository).delete(testUser);
    }

    @Test @DisplayName("delete - not found")
    void delete_NotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.delete(UUID.randomUUID()));
    }
}