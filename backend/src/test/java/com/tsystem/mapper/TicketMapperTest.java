package com.tsystem.mapper;

import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.dto.response.TicketResponse;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.mapper.TicketMapper;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketMapperTest {

    private User testUser;
    private Project testProject;
    private Ticket testTicket;
    private UUID userId;
    private UUID projectId;
    private UUID ticketId;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        now = OffsetDateTime.now();

        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.USER)
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .status(ProjectStatus.ACTIVE)
                .user(testUser)
                .build();

        testTicket = Ticket.builder()
                .id(ticketId)
                .name("Test Ticket")
                .description("Ticket Description")
                .type(TicketType.bug)
                .priority(TicketPriority.high)
                .state(TicketState.open)
                .author(testUser)
                .assignee(null)
                .project(testProject)
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("toResponse - converts all fields correctly")
    void toResponse_ConvertsAllFields() {
        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertNotNull(response);
        assertEquals(ticketId, response.getId());
        assertEquals("Test Ticket", response.getName());
        assertEquals("Ticket Description", response.getDescription());
        assertEquals(TicketType.bug, response.getType());
        assertEquals(TicketPriority.high, response.getPriority());
        assertEquals(TicketState.open, response.getState());
        assertEquals(now, response.getCreatedAt());
        assertEquals(projectId, response.getProjectId());
    }

    @Test
    @DisplayName("toResponse - converts owner correctly")
    void toResponse_ConvertsOwner() {
        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertNotNull(response.getOwner());
        assertEquals(userId, response.getOwner().getId());
        assertEquals("test@example.com", response.getOwner().getUsername());
        assertEquals("Test", response.getOwner().getName());
        assertEquals("User", response.getOwner().getSurname());
    }

    @Test
    @DisplayName("toResponse - null assignee returns null")
    void toResponse_NullAssignee() {
        testTicket.setAssignee(null);

        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertNull(response.getAssignee());
    }

    @Test
    @DisplayName("toResponse - converts assignee when present")
    void toResponse_WithAssignee() {
        UUID assigneeId = UUID.randomUUID();
        User assignee = User.builder()
                .id(assigneeId)
                .username("assignee@test.com")
                .name("Assignee")
                .surname("Person")
                .build();
        testTicket.setAssignee(assignee);

        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertNotNull(response.getAssignee());
        assertEquals(assigneeId, response.getAssignee().getId());
        assertEquals("assignee@test.com", response.getAssignee().getUsername());
        assertEquals("Assignee", response.getAssignee().getName());
        assertEquals("Person", response.getAssignee().getSurname());
    }

    @Test
    @DisplayName("toResponse - handles all ticket types")
    void toResponse_AllTicketTypes() {
        for (TicketType type : TicketType.values()) {
            testTicket.setType(type);

            TicketResponse response = TicketMapper.toResponse(testTicket);

            assertEquals(type, response.getType());
        }
    }

    @Test
    @DisplayName("toResponse - handles all ticket priorities")
    void toResponse_AllTicketPriorities() {
        for (TicketPriority priority : TicketPriority.values()) {
            testTicket.setPriority(priority);

            TicketResponse response = TicketMapper.toResponse(testTicket);

            assertEquals(priority, response.getPriority());
        }
    }

    @Test
    @DisplayName("toResponse - handles all ticket states")
    void toResponse_AllTicketStates() {
        for (TicketState state : TicketState.values()) {
            testTicket.setState(state);

            TicketResponse response = TicketMapper.toResponse(testTicket);

            assertEquals(state, response.getState());
        }
    }

    @Test
    @DisplayName("toResponse - handles null description")
    void toResponse_NullDescription() {
        testTicket.setDescription(null);

        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertNull(response.getDescription());
    }

    @Test
    @DisplayName("toResponse - includes correct projectId")
    void toResponse_IncludesProjectId() {
        TicketResponse response = TicketMapper.toResponse(testTicket);

        assertEquals(testProject.getId(), response.getProjectId());
    }
}
