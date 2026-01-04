package com.tsystem.mapper;

import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.dto.response.UserResponse;
import com.tsystem.model.mapper.UserMapper;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .password("hashedPassword")
                .role(SystemRole.USER)
                .blocked(false)
                .build();
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("converts all fields correctly")
        void toResponse_ConvertsAllFields() {
            UserResponse response = UserMapper.toResponse(testUser);

            assertNotNull(response);
            assertEquals(userId, response.getId());
            assertEquals("Test", response.getName());
            assertEquals("User", response.getSurname());
            assertEquals("test@example.com", response.getUsername());
            assertEquals("USER", response.getRole());
            assertFalse(response.isBlocked());
        }

        @Test
        @DisplayName("handles blocked user")
        void toResponse_BlockedUser() {
            testUser.setBlocked(true);

            UserResponse response = UserMapper.toResponse(testUser);

            assertTrue(response.isBlocked());
        }

        @Test
        @DisplayName("handles unblocked user")
        void toResponse_UnblockedUser() {
            testUser.setBlocked(false);

            UserResponse response = UserMapper.toResponse(testUser);

            assertFalse(response.isBlocked());
        }

        @Test
        @DisplayName("USER role converts correctly")
        void toResponse_UserRole() {
            testUser.setRole(SystemRole.USER);

            UserResponse response = UserMapper.toResponse(testUser);

            assertEquals("USER", response.getRole());
        }

        @Test
        @DisplayName("ADMIN role converts correctly")
        void toResponse_AdminRole() {
            testUser.setRole(SystemRole.ADMIN);

            UserResponse response = UserMapper.toResponse(testUser);

            assertEquals("ADMIN", response.getRole());
        }

        @Test
        @DisplayName("PROJECT_MANAGER role converts correctly")
        void toResponse_ProjectManagerRole() {
            testUser.setRole(SystemRole.PROJECT_MANAGER);

            UserResponse response = UserMapper.toResponse(testUser);

            assertEquals("PROJECT_MANAGER", response.getRole());
        }

        @Test
        @DisplayName("handles all system roles")
        void toResponse_AllRoles() {
            for (SystemRole role : SystemRole.values()) {
                testUser.setRole(role);

                UserResponse response = UserMapper.toResponse(testUser);

                assertEquals(role.toString(), response.getRole());
            }
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("updates all fields correctly")
        void update_UpdatesAllFields() {
            UserRequest req = UserRequest.builder()
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals("updated@test.com", testUser.getEmail());
            assertEquals("Updated", testUser.getName());
            assertEquals("Name", testUser.getSurname());
            assertEquals(SystemRole.ADMIN, testUser.getRole());
        }

        @Test
        @DisplayName("updates email")
        void update_UpdatesEmail() {
            UserRequest req = UserRequest.builder()
                    .email("newemail@test.com")
                    .name("Test")
                    .surname("User")
                    .role("USER")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals("newemail@test.com", testUser.getEmail());
        }

        @Test
        @DisplayName("updates name")
        void update_UpdatesName() {
            UserRequest req = UserRequest.builder()
                    .email("test@example.com")
                    .name("NewName")
                    .surname("User")
                    .role("USER")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals("NewName", testUser.getName());
        }

        @Test
        @DisplayName("updates surname")
        void update_UpdatesSurname() {
            UserRequest req = UserRequest.builder()
                    .email("test@example.com")
                    .name("Test")
                    .surname("NewSurname")
                    .role("USER")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals("NewSurname", testUser.getSurname());
        }

        @Test
        @DisplayName("changes role from USER to ADMIN")
        void update_ChangeRoleToAdmin() {
            assertEquals(SystemRole.USER, testUser.getRole());

            UserRequest req = UserRequest.builder()
                    .email("test@example.com")
                    .name("Test")
                    .surname("User")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals(SystemRole.ADMIN, testUser.getRole());
        }

        @Test
        @DisplayName("changes role from USER to PROJECT_MANAGER")
        void update_ChangeRoleToProjectManager() {
            UserRequest req = UserRequest.builder()
                    .email("test@example.com")
                    .name("Test")
                    .surname("User")
                    .role("PROJECT_MANAGER")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals(SystemRole.PROJECT_MANAGER, testUser.getRole());
        }

        @Test
        @DisplayName("preserves id")
        void update_PreservesId() {
            UUID originalId = testUser.getId();

            UserRequest req = UserRequest.builder()
                    .email("new@email.com")
                    .name("New")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals(originalId, testUser.getId());
        }

        @Test
        @DisplayName("preserves username")
        void update_PreservesUsername() {
            String originalUsername = testUser.getUsername();

            UserRequest req = UserRequest.builder()
                    .email("new@email.com")
                    .name("New")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals(originalUsername, testUser.getUsername());
        }

        @Test
        @DisplayName("preserves password")
        void update_PreservesPassword() {
            String originalPassword = testUser.getPassword();

            UserRequest req = UserRequest.builder()
                    .email("new@email.com")
                    .name("New")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertEquals(originalPassword, testUser.getPassword());
        }

        @Test
        @DisplayName("preserves blocked status")
        void update_PreservesBlockedStatus() {
            testUser.setBlocked(true);

            UserRequest req = UserRequest.builder()
                    .email("new@email.com")
                    .name("New")
                    .surname("Name")
                    .role("ADMIN")
                    .build();

            UserMapper.update(testUser, req);

            assertTrue(testUser.isBlocked());
        }
    }
}
