package com.tsystem.user;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.USER)
                .password("hashedPassword")
                .build();
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("returns all users")
        void returnsAllUsers() {
            User anotherUser = User.builder().id(UUID.randomUUID()).build();
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));

            List<User> result = userService.findAll();

            assertEquals(2, result.size());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no users")
        void returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            List<User> result = userService.findAll();

            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            User result = userService.findById(userId);

            assertEquals(testUser, result);
            assertEquals(userId, result.getId());
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("throws NotFoundException when user not found")
        void throwsNotFoundExceptionWhenNotFound() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.findById(randomId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.findById(randomId)
            );

            assertEquals("User not found.", exception.getMessage());
            verify(userRepository).findById(randomId);
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            User result = userService.findByEmail(email);

            assertEquals(testUser, result);
            assertEquals(email, result.getEmail());
            verify(userRepository).findByEmail(email);
        }

        @Test
        @DisplayName("throws NotFoundException when user not found")
        void throwsNotFoundExceptionWhenNotFound() {
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.findByEmail(email)
            );

            assertEquals("User not found.", exception.getMessage());
            verify(userRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsernameTests {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            String username = "test@example.com";
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

            User result = userService.findByUsername(username);

            assertEquals(testUser, result);
            assertEquals(username, result.getUsername());
            verify(userRepository).findByUsername(username);
        }

        @Test
        @DisplayName("throws NotFoundException when user not found")
        void throwsNotFoundExceptionWhenNotFound() {
            String username = "nonexistent";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.findByUsername(username)
            );

            assertEquals("User not found.", exception.getMessage());
            verify(userRepository).findByUsername(username);
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("creates user with USER role")
        void createsUserWithUserRole() {
            UserRequest request = UserRequest.builder()
                    .email("new@test.com")
                    .name("New")
                    .surname("User")
                    .password("password123")
                    .role("USER")
                    .build();

            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.create(request);

            assertEquals("new@test.com", result.getEmail());
            assertEquals("new@test.com", result.getUsername());
            assertEquals("New", result.getName());
            assertEquals("User", result.getSurname());
            assertEquals(SystemRole.USER, result.getRole());
            assertEquals("hashedPassword", result.getPassword());

            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("creates user with ADMIN role")
        void createsUserWithAdminRole() {
            UserRequest request = UserRequest.builder()
                    .email("admin@test.com")
                    .name("Admin")
                    .surname("User")
                    .password("adminPass")
                    .role("ADMIN")
                    .build();

            when(passwordEncoder.encode("adminPass")).thenReturn("hashedAdminPass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.create(request);

            assertEquals(SystemRole.ADMIN, result.getRole());
            verify(passwordEncoder).encode("adminPass");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("sets username equal to email")
        void setsUsernameEqualToEmail() {
            UserRequest request = UserRequest.builder()
                    .email("unique@test.com")
                    .name("Test")
                    .surname("User")
                    .password("pass")
                    .role("USER")
                    .build();

            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.create(request);

            assertEquals(result.getEmail(), result.getUsername());
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("updates user without password change")
        void updatesUserWithoutPasswordChange() {
            UserRequest request = UserRequest.builder()
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.update(userId, request);

            assertEquals("updated@test.com", result.getEmail());
            assertEquals("updated@test.com", result.getUsername());
            assertEquals("Updated", result.getName());
            assertEquals("Name", result.getSurname());
            assertEquals(SystemRole.ADMIN, result.getRole());
            assertEquals("hashedPassword", result.getPassword()); // unchanged

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("updates user with new password")
        void updatesUserWithNewPassword() {
            UserRequest request = UserRequest.builder()
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .password("newPassword")
                    .role("USER")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.update(userId, request);

            assertEquals("newHashedPassword", result.getPassword());
            verify(passwordEncoder).encode("newPassword");
        }

        @Test
        @DisplayName("does not update password when blank")
        void doesNotUpdatePasswordWhenBlank() {
            UserRequest request = UserRequest.builder()
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .password("   ") // blank password
                    .role("USER")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.update(userId, request);

            assertEquals("hashedPassword", result.getPassword()); // unchanged
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("does not update password when empty string")
        void doesNotUpdatePasswordWhenEmpty() {
            UserRequest request = UserRequest.builder()
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .password("") // empty password
                    .role("USER")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.update(userId, request);

            assertEquals("hashedPassword", result.getPassword());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("throws NotFoundException when user not found")
        void throwsNotFoundExceptionWhenNotFound() {
            UUID randomId = UUID.randomUUID();
            UserRequest request = UserRequest.builder()
                    .email("x@y.com")
                    .name("X")
                    .surname("Y")
                    .role("USER")
                    .build();

            when(userRepository.findById(randomId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.update(randomId, request)
            );

            assertEquals("User not found.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("deletes user successfully")
        void deletesUserSuccessfully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            userService.delete(userId);

            verify(userRepository).findById(userId);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("throws NotFoundException when user not found")
        void throwsNotFoundExceptionWhenNotFound() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.findById(randomId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.delete(randomId)
            );

            assertEquals("User not found.", exception.getMessage());
            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("blockUser")
    class BlockUserTests {

        @Test
        @DisplayName("blocks user successfully")
        void blocksUserSuccessfully() {
            when(userRepository.blockUser(userId)).thenReturn(1);

            userService.blockUser(userId);

            verify(userRepository).blockUser(userId);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void throwsEntityNotFoundExceptionWhenNotFound() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.blockUser(randomId)).thenReturn(0);

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> userService.blockUser(randomId)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).blockUser(randomId);
        }
    }

    @Nested
    @DisplayName("unblockUser")
    class UnblockUserTests {

        @Test
        @DisplayName("unblocks user successfully")
        void unblocksUserSuccessfully() {
            when(userRepository.unblockUser(userId)).thenReturn(1);

            userService.unblockUser(userId);

            verify(userRepository).unblockUser(userId);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void throwsEntityNotFoundExceptionWhenNotFound() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.unblockUser(randomId)).thenReturn(0);

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> userService.unblockUser(randomId)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).unblockUser(randomId);
        }
    }
}