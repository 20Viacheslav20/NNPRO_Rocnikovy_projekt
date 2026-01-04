package com.tsystem.user;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.UserController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
                .role(SystemRole.USER)
                .blocked(false)
                .build();
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("returns list of users")
        void getAllUsers_Success() throws Exception {
            when(userService.findAll()).thenReturn(List.of(testUser));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(userId.toString()))
                    .andExpect(jsonPath("$[0].username").value("test@example.com"))
                    .andExpect(jsonPath("$[0].name").value("Test"))
                    .andExpect(jsonPath("$[0].surname").value("User"))
                    .andExpect(jsonPath("$[0].role").value("USER"))
                    .andExpect(jsonPath("$[0].blocked").value(false));

            verify(userService).findAll();
        }

        @Test
        @DisplayName("returns empty list")
        void getAllUsers_Empty() throws Exception {
            when(userService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("returns multiple users")
        void getAllUsers_Multiple() throws Exception {
            User user2 = User.builder()
                    .id(UUID.randomUUID())
                    .username("user2@test.com")
                    .email("user2@test.com")
                    .name("User2")
                    .surname("Test")
                    .role(SystemRole.ADMIN)
                    .blocked(false)
                    .build();

            when(userService.findAll()).thenReturn(Arrays.asList(testUser, user2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].role").value("USER"))
                    .andExpect(jsonPath("$[1].role").value("ADMIN"));
        }

        @Test
        @DisplayName("returns blocked user in list")
        void getAllUsers_IncludesBlockedUser() throws Exception {
            testUser.setBlocked(true);
            when(userService.findAll()).thenReturn(List.of(testUser));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].blocked").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/users/getById/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("returns user by id")
        void getUserById_Success() throws Exception {
            when(userService.findById(userId)).thenReturn(testUser);

            mockMvc.perform(get("/api/users/getById/{id}", userId))
                    .andExpect(status().isOk());

            verify(userService).findById(userId);
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void getUserById_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            when(userService.findById(randomId)).thenThrow(new NotFoundException("User not found."));

            mockMvc.perform(get("/api/users/getById/{id}", randomId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUserTests {

        @Test
        @DisplayName("creates user successfully")
        void create_Success() throws Exception {
            when(userService.create(any())).thenReturn(testUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "name": "Test",
                                        "surname": "User",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("test@example.com"))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(userService).create(any());
        }

        @Test
        @DisplayName("creates admin user")
        void create_AdminUser() throws Exception {
            User adminUser = User.builder()
                    .id(UUID.randomUUID())
                    .username("admin@example.com")
                    .email("admin@example.com")
                    .name("Admin")
                    .surname("User")
                    .role(SystemRole.ADMIN)
                    .build();

            when(userService.create(any())).thenReturn(adminUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "admin@example.com",
                                        "name": "Admin",
                                        "surname": "User",
                                        "password": "password123",
                                        "role": "ADMIN"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("returns 400 when email is missing")
        void create_MissingEmail_Returns400() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test",
                                        "surname": "User",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(any());
        }

        @Test
        @DisplayName("returns 400 when password is too short")
        void create_ShortPassword_Returns400() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "name": "Test",
                                        "surname": "User",
                                        "password": "123",
                                        "role": "USER"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUserTests {

        @Test
        @DisplayName("updates user successfully")
        void update_Success() throws Exception {
            User updated = User.builder()
                    .id(userId)
                    .username("updated@test.com")
                    .email("updated@test.com")
                    .name("Updated")
                    .surname("Name")
                    .role(SystemRole.ADMIN)
                    .build();

            when(userService.update(eq(userId), any())).thenReturn(updated);

            mockMvc.perform(put("/api/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "updated@test.com",
                                        "name": "Updated",
                                        "surname": "Name",
                                        "role": "ADMIN"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updated@test.com"))
                    .andExpect(jsonPath("$.name").value("Updated"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            verify(userService).update(eq(userId), any());
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void update_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            when(userService.update(eq(randomId), any()))
                    .thenThrow(new NotFoundException("User not found."));

            mockMvc.perform(put("/api/users/{id}", randomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "x@y.com",
                                        "name": "X",
                                        "surname": "Y",
                                        "role": "USER"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updates user without changing password")
        void update_WithoutPassword() throws Exception {
            when(userService.update(eq(userId), any())).thenReturn(testUser);

            mockMvc.perform(put("/api/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "name": "Test",
                                        "surname": "User",
                                        "role": "USER"
                                    }
                                    """))
                    .andExpect(status().isOk());

            verify(userService).update(eq(userId), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUserTests {

        @Test
        @DisplayName("deletes user successfully")
        void delete_Success() throws Exception {
            doNothing().when(userService).delete(userId);

            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            verify(userService).delete(userId);
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void delete_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            doThrow(new NotFoundException("User not found."))
                    .when(userService).delete(randomId);

            mockMvc.perform(delete("/api/users/{id}", randomId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users/{id}/block")
    class BlockUserTests {

        @Test
        @DisplayName("blocks user successfully")
        void blockUser_Success() throws Exception {
            doNothing().when(userService).blockUser(userId);

            mockMvc.perform(post("/api/users/{id}/block", userId))
                    .andExpect(status().isNoContent());

            verify(userService).blockUser(userId);
        }

        @Test
        @DisplayName("blocks already blocked user (idempotent)")
        void blockUser_AlreadyBlocked() throws Exception {
            doNothing().when(userService).blockUser(userId);

            mockMvc.perform(post("/api/users/{id}/block", userId))
                    .andExpect(status().isNoContent());

            verify(userService).blockUser(userId);
        }
    }

    @Nested
    @DisplayName("POST /api/users/{id}/unblock")
    class UnblockUserTests {

        @Test
        @DisplayName("unblocks user successfully")
        void unblockUser_Success() throws Exception {
            doNothing().when(userService).unblockUser(userId);

            mockMvc.perform(post("/api/users/{id}/unblock", userId))
                    .andExpect(status().isNoContent());

            verify(userService).unblockUser(userId);
        }

        @Test
        @DisplayName("unblocks already unblocked user (idempotent)")
        void unblockUser_AlreadyUnblocked() throws Exception {
            doNothing().when(userService).unblockUser(userId);

            mockMvc.perform(post("/api/users/{id}/unblock", userId))
                    .andExpect(status().isNoContent());

            verify(userService).unblockUser(userId);
        }
    }
}