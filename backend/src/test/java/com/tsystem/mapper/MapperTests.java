package com.tsystem.mapper;

import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.dto.response.ProjectResponse;
import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.dto.response.UserResponse;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.mapper.ProjectMapper;
import com.tsystem.model.mapper.TicketMapper;
import com.tsystem.model.mapper.UserMapper;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

    private User testUser;
    private Project testProject;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).username("test@example.com").email("test@example.com")
                .name("Test").surname("User").role(SystemRole.USER).build();
        testProject = Project.builder().id(UUID.randomUUID()).name("Test Project").description("Test Description")
                .status(ProjectStatus.ACTIVE).user(testUser).createdAt(OffsetDateTime.now()).build();
        testTicket = Ticket.builder().id(UUID.randomUUID()).name("Test Ticket").description("Ticket Description")
                .type(TicketType.bug).priority(TicketPriority.high).state(TicketState.open)
                .author(testUser).project(testProject).createdAt(OffsetDateTime.now()).build();
    }

    // ProjectMapper Tests
    @Test @DisplayName("ProjectMapper.toResponse - converts all fields")
    void projectMapper_toResponse_ConvertsAllFields() {
        ProjectResponse response = ProjectMapper.toResponse(testProject);
        assertEquals(testProject.getId(), response.getId());
        assertEquals(testProject.getName(), response.getName());
        assertEquals(testProject.getDescription(), response.getDescription());
        assertEquals(testProject.getStatus(), response.getStatus());
        assertNotNull(response.getOwner());
    }

    @Test @DisplayName("ProjectMapper.toResponse - converts owner")
    void projectMapper_toResponse_ConvertsOwner() {
        ProjectResponse response = ProjectMapper.toResponse(testProject);
        assertEquals(testUser.getId(), response.getOwner().getId());
        assertEquals(testUser.getUsername(), response.getOwner().getUsername());
    }

    // TicketMapper Tests
    @Test @DisplayName("TicketMapper.toResponse - converts all fields")
    void ticketMapper_toResponse_ConvertsAllFields() {
        TicketResponse response = TicketMapper.toResponse(testTicket);
        assertEquals(testTicket.getId(), response.getId());
        assertEquals(testTicket.getName(), response.getName());
        assertEquals(testTicket.getType(), response.getType());
        assertEquals(testTicket.getPriority(), response.getPriority());
        assertEquals(testTicket.getState(), response.getState());
    }

    @Test @DisplayName("TicketMapper.toResponse - null assignee")
    void ticketMapper_toResponse_NullAssignee() {
        TicketResponse response = TicketMapper.toResponse(testTicket);
        assertNull(response.getAssignee());
    }

    @Test @DisplayName("TicketMapper.toResponse - with assignee")
    void ticketMapper_toResponse_WithAssignee() {
        User assignee = User.builder().id(UUID.randomUUID()).username("assignee@test.com").name("A").surname("U").build();
        testTicket.setAssignee(assignee);
        TicketResponse response = TicketMapper.toResponse(testTicket);
        assertNotNull(response.getAssignee());
        assertEquals(assignee.getUsername(), response.getAssignee().getUsername());
    }

    @Test @DisplayName("TicketMapper.toResponse - includes projectId")
    void ticketMapper_toResponse_IncludesProjectId() {
        TicketResponse response = TicketMapper.toResponse(testTicket);
        assertEquals(testProject.getId(), response.getProjectId());
    }

    // UserMapper Tests
    @Test @DisplayName("UserMapper.toResponse - converts all fields")
    void userMapper_toResponse_ConvertsAllFields() {
        UserResponse response = UserMapper.toResponse(testUser);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals("USER", response.getRole());
    }

    @Test @DisplayName("UserMapper.toResponse - ADMIN role")
    void userMapper_toResponse_AdminRole() {
        testUser.setRole(SystemRole.ADMIN);
        UserResponse response = UserMapper.toResponse(testUser);
        assertEquals("ADMIN", response.getRole());
    }

    @Test @DisplayName("UserMapper.toResponse - PROJECT_MANAGER role")
    void userMapper_toResponse_ProjectManagerRole() {
        testUser.setRole(SystemRole.PROJECT_MANAGER);
        UserResponse response = UserMapper.toResponse(testUser);
        assertEquals("PROJECT_MANAGER", response.getRole());
    }

    @Test @DisplayName("UserMapper.update - updates all fields")
    void userMapper_update_UpdatesAllFields() {
        UserRequest req = UserRequest.builder().email("upd@test.com").name("Updated").surname("Name").role("ADMIN").build();
        UserMapper.update(testUser, req);
        assertEquals("upd@test.com", testUser.getEmail());
        assertEquals("Updated", testUser.getName());
        assertEquals(SystemRole.ADMIN, testUser.getRole());
    }
}
