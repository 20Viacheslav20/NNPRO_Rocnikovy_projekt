package com.tsystem.user;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.UserController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder().id(userId).username("test@example.com").email("test@example.com")
                .name("Test").surname("User").role(SystemRole.USER).build();
    }

    @Test @DisplayName("GET /api/users - returns list")
    void getAllUsers_Success() throws Exception {
        when(userService.findAll()).thenReturn(List.of(testUser));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("test@example.com"));
    }

    @Test @DisplayName("GET /api/users - empty list")
    void getAllUsers_Empty() throws Exception {
        when(userService.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test @DisplayName("GET /api/users - a few users")
    void getAllUsers_Multiple() throws Exception {
        User user2 = User.builder().id(UUID.randomUUID()).username("user2@test.com").name("U2").surname("T").role(SystemRole.ADMIN).build();
        when(userService.findAll()).thenReturn(Arrays.asList(testUser, user2));
        mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test @DisplayName("GET /api/users/getById/{id} - successfully")
    void getUserById_Success() throws Exception {
        when(userService.findById(userId)).thenReturn(testUser);
        mockMvc.perform(get("/api/users/getById/{id}", userId)).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/users/getById/{id} - 404")
    void getUserById_NotFound() throws Exception {
        when(userService.findById(any())).thenThrow(new NotFoundException("User not found."));
        mockMvc.perform(get("/api/users/getById/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /api/users - creates")
    void create_Success() throws Exception {
        when(userService.create(any())).thenReturn(testUser);
        // password must contain at least 6 symbols according to @Size(min = 6)
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"name\":\"Test\",\"surname\":\"User\",\"password\":\"password123\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("test@example.com"));
    }

    @Test @DisplayName("PUT /api/users/{id} - updates")
    void update_Success() throws Exception {
        User updated = User.builder().id(userId).username("upd@test.com").email("upd@test.com")
                .name("Updated").surname("Name").role(SystemRole.ADMIN).build();
        when(userService.update(eq(userId), any())).thenReturn(updated);
        mockMvc.perform(put("/api/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"upd@test.com\",\"name\":\"Updated\",\"surname\":\"Name\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("upd@test.com"));
    }

    @Test @DisplayName("PUT /api/users/{id} - 404")
    void update_NotFound() throws Exception {
        when(userService.update(any(), any())).thenThrow(new NotFoundException("User not found."));
        mockMvc.perform(put("/api/users/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@y.com\",\"name\":\"X\",\"surname\":\"Y\",\"role\":\"USER\"}"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE /api/users/{id} - removes")
    void delete_Success() throws Exception {
        doNothing().when(userService).delete(userId);
        mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNoContent());
        verify(userService).delete(userId);
    }

    @Test @DisplayName("DELETE /api/users/{id} - 404")
    void delete_NotFound() throws Exception {
        doThrow(new NotFoundException("User not found.")).when(userService).delete(any());
        mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }
}

