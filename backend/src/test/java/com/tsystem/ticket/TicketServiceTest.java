package com.tsystem.ticket;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.TicketComment;
import com.tsystem.model.TicketHistory;
import com.tsystem.model.dto.request.TicketCommentRequest;
import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.*;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock TicketCommentRepository ticketCommentRepository;
    @Mock TicketHistoryRepository ticketHistoryRepository;

    @InjectMocks TicketService ticketService;

    private User adminUser, managerUser, regularUser;
    private Project testProject;
    private Ticket testTicket;
    private UUID projectId, ticketId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        adminUser = User.builder().id(UUID.randomUUID()).username("admin@test.com").role(SystemRole.ADMIN).build();
        managerUser = User.builder().id(UUID.randomUUID()).username("manager@test.com").role(SystemRole.PROJECT_MANAGER).build();
        regularUser = User.builder().id(UUID.randomUUID()).username("user@test.com").role(SystemRole.USER).build();

        testProject = Project.builder().id(projectId).name("Test Project").build();
        testTicket = Ticket.builder()
                .id(ticketId).name("Test Ticket").description("Description")
                .type(TicketType.bug).priority(TicketPriority.high).state(TicketState.open)
                .project(testProject).author(adminUser).assignee(regularUser).build();
    }

    @Nested
    @DisplayName("Create Ticket Tests")
    class CreateTests {
        @Test
        @DisplayName("Successful ticket creation")
        void create_Success() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("New Bug").type(TicketType.bug).priority(TicketPriority.high).build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> {
                Ticket t = i.getArgument(0);
                t.setId(UUID.randomUUID());
                return t;
            });
            when(ticketHistoryRepository.save(any(TicketHistory.class))).thenAnswer(i -> i.getArgument(0));

            Ticket result = ticketService.create(projectId, req, "admin@test.com");

            assertEquals("New Bug", result.getName());
            assertEquals(adminUser, result.getAuthor());
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("Ticket creation - project not found")
        void create_ProjectNotFound() {
            TicketCreateRequest req = TicketCreateRequest.builder().name("Bug").type(TicketType.bug).priority(TicketPriority.low).build();
            when(projectRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> ticketService.create(projectId, req, "admin@test.com"));
        }

        @Test
        @DisplayName("Ticket creation - user not found")
        void create_UserNotFound() {
            TicketCreateRequest req = TicketCreateRequest.builder().name("Bug").type(TicketType.bug).priority(TicketPriority.low).build();
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.create(projectId, req, "unknown@test.com"));
        }
    }

    @Nested
    @DisplayName("Get Ticket Tests")
    class GetTests {
        @Test
        @DisplayName("Get ticket successfully")
        void get_Success() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));

            Ticket result = ticketService.get(projectId, ticketId);

            assertEquals(testTicket, result);
        }

        @Test
        @DisplayName("Ticket not found")
        void get_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.get(projectId, ticketId));
        }
    }

    @Nested
    @DisplayName("Get All By Project Tests")
    class GetAllByProjectTests {
        @Test
        @DisplayName("Returns all tickets for project")
        void getAllByProjectId_Success() {
            Ticket ticket2 = Ticket.builder().id(UUID.randomUUID()).name("Ticket 2").project(testProject).build();
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(ticketRepository.findByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(Arrays.asList(testTicket, ticket2));

            List<Ticket> result = ticketService.getAllByProjectId(projectId);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Project not found")
        void getAllByProjectId_ProjectNotFound() {
            when(projectRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> ticketService.getAllByProjectId(projectId));
        }
    }

    @Nested
    @DisplayName("Find By Assignee Tests")
    class FindByAssigneeTests {
        @Test
        @DisplayName("Returns assigned tickets")
        void findByAssignee_Success() {
            when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
            when(ticketRepository.findByAssigneeId(regularUser.getId())).thenReturn(List.of(testTicket));

            List<Ticket> result = ticketService.findByAssignee(regularUser.getId());

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("User not found")
        void findByAssignee_UserNotFound() {
            when(userRepository.findById(any())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> ticketService.findByAssignee(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("Update Ticket Tests")
    class UpdateTests {
        @Test
        @DisplayName("Success ticket update")
        void update_Success() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Updated").description("New desc")
                    .priority(TicketPriority.low).state(TicketState.in_progress).build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketHistoryRepository.save(any(TicketHistory.class))).thenAnswer(i -> i.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "admin@test.com");

            assertEquals("Updated", result.getName());
            assertEquals(TicketState.in_progress, result.getState());
            verify(ticketHistoryRepository, atLeastOnce()).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("Update non-existing ticket")
        void update_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());
            TicketUpdateRequest req = TicketUpdateRequest.builder().name("X").build();

            assertThrows(NotFoundException.class, () -> ticketService.update(projectId, ticketId, req, "admin@test.com"));
        }

        @Test
        @DisplayName("Update with unknown user")
        void update_UserNotFound() {
            TicketUpdateRequest req = TicketUpdateRequest.builder().name("Updated").build();
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.update(projectId, ticketId, req, "unknown@test.com"));
        }

        @Test
        @DisplayName("Update logs history for each changed field")
        void update_LogsHistoryForChanges() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("New Name")
                    .description("New Description")
                    .priority(TicketPriority.low)
                    .state(TicketState.done).build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketHistoryRepository.save(any(TicketHistory.class))).thenAnswer(i -> i.getArgument(0));

            ticketService.update(projectId, ticketId, req, "admin@test.com");

            // Should log 4 changes: name, description, priority, state
            verify(ticketHistoryRepository, times(4)).save(any(TicketHistory.class));
        }
    }

    @Nested
    @DisplayName("Delete Ticket Tests")
    class DeleteTests {
        @Test
        @DisplayName("Successful ticket removal")
        void delete_Success() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(ticketHistoryRepository.save(any(TicketHistory.class))).thenAnswer(i -> i.getArgument(0));
            doNothing().when(ticketRepository).delete(testTicket);

            ticketService.delete(projectId, ticketId, "admin@test.com");

            verify(ticketRepository).delete(testTicket);
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("Non-existing ticket removal")
        void delete_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.delete(projectId, ticketId, "admin@test.com"));
        }
    }

    @Nested
    @DisplayName("Comments Tests")
    class CommentsTests {
        private TicketComment testComment;
        private UUID commentId;

        @BeforeEach
        void setUpComments() {
            commentId = UUID.randomUUID();
            testComment = TicketComment.builder()
                    .id(commentId)
                    .ticketId(ticketId)
                    .authorId(adminUser.getId())
                    .text("Test comment")
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Get comments for ticket")
        void getComments_Success() {
            when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(List.of(testComment));

            List<TicketComment> result = ticketService.getComments(ticketId);

            assertEquals(1, result.size());
            assertEquals("Test comment", result.get(0).getText());
        }

        @Test
        @DisplayName("Get comments - empty list")
        void getComments_Empty() {
            when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(List.of());

            List<TicketComment> result = ticketService.getComments(ticketId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Add comment successfully")
        void addComment_Success() {
            TicketCommentRequest req = TicketCommentRequest.builder()
                    .text("New comment").build();

            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(i -> {
                TicketComment c = i.getArgument(0);
                c.setId(UUID.randomUUID());
                return c;
            });

            TicketComment result = ticketService.addComment(ticketId, req, "admin@test.com");

            assertEquals("New comment", result.getText());
            assertEquals(ticketId, result.getTicketId());
            assertEquals(adminUser.getId(), result.getAuthorId());
        }

        @Test
        @DisplayName("Add comment - user not found")
        void addComment_UserNotFound() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("Comment").build();
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.addComment(ticketId, req, "unknown@test.com"));
        }

        @Test
        @DisplayName("Update comment successfully")
        void updateComment_Success() {
            TicketCommentRequest req = TicketCommentRequest.builder()
                    .text("Updated comment").build();

            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
            when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(i -> i.getArgument(0));

            TicketComment result = ticketService.updateComment(commentId, req);

            assertEquals("Updated comment", result.getText());
        }

        @Test
        @DisplayName("Update comment - not found")
        void updateComment_NotFound() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("Updated").build();
            when(ticketCommentRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.updateComment(commentId, req));
        }

        @Test
        @DisplayName("Delete comment successfully")
        void deleteComment_Success() {
            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
            doNothing().when(ticketCommentRepository).delete(testComment);

            ticketService.deleteComment(commentId);

            verify(ticketCommentRepository).delete(testComment);
        }

        @Test
        @DisplayName("Delete comment - not found")
        void deleteComment_NotFound() {
            when(ticketCommentRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.deleteComment(commentId));
        }
    }

    @Nested
    @DisplayName("History Tests")
    class HistoryTests {
        @Test
        @DisplayName("Get history for ticket")
        void getHistory_Success() {
            TicketHistory history1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(adminUser.getId())
                    .action("CREATED")
                    .createdAt(OffsetDateTime.now())
                    .build();

            TicketHistory history2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(adminUser.getId())
                    .action("UPDATED")
                    .field("state")
                    .oldValue("open")
                    .newValue("in_progress")
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(List.of(history1, history2));

            List<TicketHistory> result = ticketService.getHistory(ticketId);

            assertEquals(2, result.size());
            assertEquals("CREATED", result.get(0).getAction());
            assertEquals("UPDATED", result.get(1).getAction());
        }

        @Test
        @DisplayName("Get history - empty")
        void getHistory_Empty() {
            when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(List.of());

            List<TicketHistory> result = ticketService.getHistory(ticketId);

            assertTrue(result.isEmpty());
        }
    }
}