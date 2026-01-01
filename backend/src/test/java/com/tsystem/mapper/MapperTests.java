package com.tsystem.mapper;

import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.TicketComment;
import com.tsystem.model.TicketHistory;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.dto.response.*;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.mapper.*;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Nested
    @DisplayName("ProjectMapper Tests")
    class ProjectMapperTests {
        @Test
        @DisplayName("toResponse - converts all fields")
        void toResponse_ConvertsAllFields() {
            ProjectResponse response = ProjectMapper.toResponse(testProject);
            assertEquals(testProject.getId(), response.getId());
            assertEquals(testProject.getName(), response.getName());
            assertEquals(testProject.getDescription(), response.getDescription());
            assertEquals(testProject.getStatus(), response.getStatus());
            assertNotNull(response.getOwner());
        }

        @Test
        @DisplayName("toResponse - converts owner")
        void toResponse_ConvertsOwner() {
            ProjectResponse response = ProjectMapper.toResponse(testProject);
            assertEquals(testUser.getId(), response.getOwner().getId());
            assertEquals(testUser.getUsername(), response.getOwner().getUsername());
        }
    }

    @Nested
    @DisplayName("TicketMapper Tests")
    class TicketMapperTests {
        @Test
        @DisplayName("toResponse - converts all fields")
        void toResponse_ConvertsAllFields() {
            TicketResponse response = TicketMapper.toResponse(testTicket);
            assertEquals(testTicket.getId(), response.getId());
            assertEquals(testTicket.getName(), response.getName());
            assertEquals(testTicket.getType(), response.getType());
            assertEquals(testTicket.getPriority(), response.getPriority());
            assertEquals(testTicket.getState(), response.getState());
        }

        @Test
        @DisplayName("toResponse - null assignee")
        void toResponse_NullAssignee() {
            TicketResponse response = TicketMapper.toResponse(testTicket);
            assertNull(response.getAssignee());
        }

        @Test
        @DisplayName("toResponse - with assignee")
        void toResponse_WithAssignee() {
            User assignee = User.builder().id(UUID.randomUUID()).username("assignee@test.com").name("A").surname("U").build();
            testTicket.setAssignee(assignee);
            TicketResponse response = TicketMapper.toResponse(testTicket);
            assertNotNull(response.getAssignee());
            assertEquals(assignee.getUsername(), response.getAssignee().getUsername());
        }

        @Test
        @DisplayName("toResponse - includes projectId")
        void toResponse_IncludesProjectId() {
            TicketResponse response = TicketMapper.toResponse(testTicket);
            assertEquals(testProject.getId(), response.getProjectId());
        }
    }

    @Nested
    @DisplayName("UserMapper Tests")
    class UserMapperTests {
        @Test
        @DisplayName("toResponse - converts all fields")
        void toResponse_ConvertsAllFields() {
            UserResponse response = UserMapper.toResponse(testUser);
            assertEquals(testUser.getId(), response.getId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getUsername(), response.getUsername());
            assertEquals("USER", response.getRole());
        }

        @Test
        @DisplayName("toResponse - ADMIN role")
        void toResponse_AdminRole() {
            testUser.setRole(SystemRole.ADMIN);
            UserResponse response = UserMapper.toResponse(testUser);
            assertEquals("ADMIN", response.getRole());
        }

        @Test
        @DisplayName("toResponse - PROJECT_MANAGER role")
        void toResponse_ProjectManagerRole() {
            testUser.setRole(SystemRole.PROJECT_MANAGER);
            UserResponse response = UserMapper.toResponse(testUser);
            assertEquals("PROJECT_MANAGER", response.getRole());
        }

        @Test
        @DisplayName("update - updates all fields")
        void update_UpdatesAllFields() {
            UserRequest req = UserRequest.builder().email("upd@test.com").name("Updated").surname("Name").role("ADMIN").build();
            UserMapper.update(testUser, req);
            assertEquals("upd@test.com", testUser.getEmail());
            assertEquals("Updated", testUser.getName());
            assertEquals(SystemRole.ADMIN, testUser.getRole());
        }
    }

    @Nested
    @DisplayName("TicketCommentMapper Tests")
    class TicketCommentMapperTests {
        private TicketComment testComment;
        private UUID ticketId, commentId, authorId;

        @BeforeEach
        void setUp() {
            ticketId = UUID.randomUUID();
            commentId = UUID.randomUUID();
            authorId = UUID.randomUUID();
            testComment = TicketComment.builder()
                    .id(commentId)
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("Test comment text")
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("toResponse - converts all fields")
        void toResponse_ConvertsAllFields() {
            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertEquals(commentId, response.getId());
            assertEquals(ticketId, response.getTicketId());
            assertEquals(authorId, response.getCommentAuthorId());
            assertEquals("Test comment text", response.getText());
        }

        @Test
        @DisplayName("toResponseList - converts list of comments")
        void toResponseList_ConvertsList() {
            TicketComment comment2 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("Second comment")
                    .build();

            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(List.of(testComment, comment2));

            assertEquals(2, responses.size());
            assertEquals("Test comment text", responses.get(0).getText());
            assertEquals("Second comment", responses.get(1).getText());
        }

        @Test
        @DisplayName("toResponseList - empty list")
        void toResponseList_EmptyList() {
            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(List.of());

            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("TicketHistoryMapper Tests")
    class TicketHistoryMapperTests {
        private TicketHistory testHistory;
        private UUID historyId, ticketId, authorId;
        private OffsetDateTime createdAt;

        @BeforeEach
        void setUp() {
            historyId = UUID.randomUUID();
            ticketId = UUID.randomUUID();
            authorId = UUID.randomUUID();
            createdAt = OffsetDateTime.now();

            testHistory = TicketHistory.builder()
                    .id(historyId)
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("CREATED")
                    .field(null)
                    .oldValue(null)
                    .newValue(null)
                    .createdAt(createdAt)
                    .build();
        }

        @Test
        @DisplayName("toResponse - converts CREATED action")
        void toResponse_CreatedAction() {
            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals(historyId, response.getId());
            assertEquals(authorId, response.getAuthorId());
            assertEquals("CREATED", response.getAction());
            assertNull(response.getField());
            assertNull(response.getOldValue());
            assertNull(response.getNewValue());
            assertEquals(createdAt, response.getCreatedAt());
        }

        @Test
        @DisplayName("toResponse - converts UPDATED action with field changes")
        void toResponse_UpdatedAction() {
            testHistory.setAction("UPDATED");
            testHistory.setField("state");
            testHistory.setOldValue("open");
            testHistory.setNewValue("in_progress");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("UPDATED", response.getAction());
            assertEquals("state", response.getField());
            assertEquals("open", response.getOldValue());
            assertEquals("in_progress", response.getNewValue());
        }

        @Test
        @DisplayName("toResponse - converts DELETED action")
        void toResponse_DeletedAction() {
            testHistory.setAction("DELETED");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("DELETED", response.getAction());
        }

        @Test
        @DisplayName("toResponseList - converts list of history")
        void toResponseList_ConvertsList() {
            TicketHistory history2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("priority")
                    .oldValue("low")
                    .newValue("high")
                    .createdAt(OffsetDateTime.now())
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(List.of(testHistory, history2));

            assertEquals(2, responses.size());
            assertEquals("CREATED", responses.get(0).getAction());
            assertEquals("UPDATED", responses.get(1).getAction());
            assertEquals("priority", responses.get(1).getField());
        }

        @Test
        @DisplayName("toResponseList - empty list")
        void toResponseList_EmptyList() {
            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(List.of());

            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("toResponse - all field types")
        void toResponse_AllFieldTypes() {
            // Test with different field values
            testHistory.setAction("UPDATED");
            testHistory.setField("name");
            testHistory.setOldValue("Old Name");
            testHistory.setNewValue("New Name");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("name", response.getField());
            assertEquals("Old Name", response.getOldValue());
            assertEquals("New Name", response.getNewValue());
        }
    }
}