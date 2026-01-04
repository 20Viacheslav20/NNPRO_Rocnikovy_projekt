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
import com.tsystem.model.user.User;
import com.tsystem.repository.*;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Project testProject;
    private Ticket testTicket;
    private UUID userId;
    private UUID projectId;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        testTicket = Ticket.builder()
                .id(ticketId)
                .name("Test Ticket")
                .description("Test Description")
                .type(TicketType.bug)
                .priority(TicketPriority.high)
                .state(TicketState.open)
                .project(testProject)
                .author(testUser)
                .build();
    }

    @Nested
    @DisplayName("Create Ticket Tests")
    class CreateTests {

        @Test
        @DisplayName("Successfully creates ticket")
        void create_Success() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("New Ticket")
                    .description("New Description")
                    .type(TicketType.feature)
                    .priority(TicketPriority.med)
                    .build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(ticketId);
                return t;
            });

            Ticket result = ticketService.create(projectId, req, "test@example.com");

            assertEquals("New Ticket", result.getName());
            assertEquals("New Description", result.getDescription());
            assertEquals(TicketType.feature, result.getType());
            assertEquals(TicketPriority.med, result.getPriority());
            assertEquals(testProject, result.getProject());
            assertEquals(testUser, result.getAuthor());

            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("Create ticket logs CREATED action in history")
        void create_LogsHistory() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("New Ticket")
                    .description("Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.low)
                    .build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(ticketId);
                return t;
            });

            ticketService.create(projectId, req, "test@example.com");

            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());

            TicketHistory history = historyCaptor.getValue();
            assertEquals("CREATED", history.getAction());
            assertEquals(ticketId, history.getTicketId());
            assertEquals(userId, history.getAuthorId());
            assertNull(history.getField());
            assertNull(history.getOldValue());
            assertNull(history.getNewValue());
        }

        @Test
        @DisplayName("Create ticket with non-existent project throws exception")
        void create_ProjectNotFound_ThrowsException() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("Ticket")
                    .description("Desc")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ticketService.create(projectId, req, "test@example.com"));
            assertEquals("Project not found", ex.getMessage());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create ticket with non-existent user throws exception")
        void create_UserNotFound_ThrowsException() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("Ticket")
                    .description("Desc")
                    .type(TicketType.task)
                    .priority(TicketPriority.high)
                    .build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.create(projectId, req, "unknown@example.com"));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create ticket with all ticket types")
        void create_AllTicketTypes() {
            for (TicketType type : TicketType.values()) {
                TicketCreateRequest req = TicketCreateRequest.builder()
                        .name("Ticket " + type.name())
                        .type(type)
                        .priority(TicketPriority.med)
                        .build();

                when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
                when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
                when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                    Ticket t = inv.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

                Ticket result = ticketService.create(projectId, req, "test@example.com");
                assertEquals(type, result.getType());
            }
        }
    }

    @Nested
    @DisplayName("Find By Assignee Tests")
    class FindByAssigneeTests {

        @Test
        @DisplayName("Returns tickets for assignee")
        void findByAssignee_ReturnsTickets() {
            List<Ticket> tickets = Arrays.asList(testTicket);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(ticketRepository.findByAssigneeId(userId)).thenReturn(tickets);

            List<Ticket> result = ticketService.findByAssignee(userId);

            assertEquals(1, result.size());
            assertEquals(testTicket, result.get(0));
            verify(userRepository).findById(userId);
            verify(ticketRepository).findByAssigneeId(userId);
        }

        @Test
        @DisplayName("Returns empty list when no tickets assigned")
        void findByAssignee_ReturnsEmptyList() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(ticketRepository.findByAssigneeId(userId)).thenReturn(Collections.emptyList());

            List<Ticket> result = ticketService.findByAssignee(userId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Throws exception when user not found")
        void findByAssignee_UserNotFound_ThrowsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> ticketService.findByAssignee(userId));
            assertEquals("User not found", ex.getMessage());
            verify(ticketRepository, never()).findByAssigneeId(any());
        }
    }

    @Nested
    @DisplayName("Get All By Project ID Tests")
    class GetAllByProjectTests {

        @Test
        @DisplayName("Returns all tickets for project")
        void getAllByProjectId_ReturnsTickets() {
            Ticket anotherTicket = Ticket.builder()
                    .id(UUID.randomUUID())
                    .name("Another Ticket")
                    .type(TicketType.task)
                    .priority(TicketPriority.low)
                    .state(TicketState.in_progress)
                    .build();
            List<Ticket> tickets = Arrays.asList(testTicket, anotherTicket);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(ticketRepository.findByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(tickets);

            List<Ticket> result = ticketService.getAllByProjectId(projectId);

            assertEquals(2, result.size());
            verify(projectRepository).findById(projectId);
            verify(ticketRepository).findByProjectIdOrderByCreatedAtDesc(projectId);
        }

        @Test
        @DisplayName("Returns empty list when no tickets")
        void getAllByProjectId_ReturnsEmptyList() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(ticketRepository.findByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(Collections.emptyList());

            List<Ticket> result = ticketService.getAllByProjectId(projectId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Throws exception when project not found")
        void getAllByProjectId_ProjectNotFound_ThrowsException() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ticketService.getAllByProjectId(projectId));
            assertEquals("Project not found", ex.getMessage());
            verify(ticketRepository, never()).findByProjectIdOrderByCreatedAtDesc(any());
        }
    }

    @Nested
    @DisplayName("Get Single Ticket Tests")
    class GetTests {

        @Test
        @DisplayName("Returns ticket when found")
        void get_ReturnsTicket() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));

            Ticket result = ticketService.get(projectId, ticketId);

            assertEquals(testTicket, result);
            assertEquals(ticketId, result.getId());
            verify(ticketRepository).findByIdAndProjectId(ticketId, projectId);
        }

        @Test
        @DisplayName("Throws exception when ticket not found")
        void get_NotFound_ThrowsException() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> ticketService.get(projectId, ticketId));
            assertEquals("Ticket not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Update Ticket Tests")
    class UpdateTests {

        @Test
        @DisplayName("Updates ticket name and logs history")
        void update_Name_LogsHistory() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Updated Name")
                    .description("Test Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "test@example.com");

            assertEquals("Updated Name", result.getName());

            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());

            TicketHistory history = historyCaptor.getValue();
            assertEquals("UPDATED", history.getAction());
            assertEquals("name", history.getField());
            assertEquals("Test Ticket", history.getOldValue());
            assertEquals("Updated Name", history.getNewValue());
        }

        @Test
        @DisplayName("Updates ticket description and logs history")
        void update_Description_LogsHistory() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Test Ticket")
                    .description("Updated Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "test@example.com");

            assertEquals("Updated Description", result.getDescription());

            verify(ticketHistoryRepository).save(argThat(h ->
                    "UPDATED".equals(h.getAction()) && "description".equals(h.getField())
            ));
        }

        @Test
        @DisplayName("Updates ticket priority and logs history")
        void update_Priority_LogsHistory() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Test Ticket")
                    .description("Test Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.low)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "test@example.com");

            assertEquals(TicketPriority.low, result.getPriority());

            verify(ticketHistoryRepository).save(argThat(h ->
                    "UPDATED".equals(h.getAction()) &&
                            "priority".equals(h.getField()) &&
                            "high".equals(h.getOldValue()) &&
                            "low".equals(h.getNewValue())
            ));
        }

        @Test
        @DisplayName("Updates ticket state and logs history")
        void update_State_LogsHistory() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Test Ticket")
                    .description("Test Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.done)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "test@example.com");

            assertEquals(TicketState.done, result.getState());

            verify(ticketHistoryRepository).save(argThat(h ->
                    "UPDATED".equals(h.getAction()) &&
                            "state".equals(h.getField()) &&
                            "open".equals(h.getOldValue()) &&
                            "done".equals(h.getNewValue())
            ));
        }

        @Test
        @DisplayName("No history logged when no changes")
        void update_NoChanges_NoHistoryLogged() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Test Ticket")
                    .description("Test Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            ticketService.update(projectId, ticketId, req, "test@example.com");

            verify(ticketHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Updates multiple fields and logs each change")
        void update_MultipleFields_LogsAllChanges() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("New Name")
                    .description("New Description")
                    .type(TicketType.bug)
                    .priority(TicketPriority.low)
                    .state(TicketState.in_progress)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req, "test@example.com");

            assertEquals("New Name", result.getName());
            assertEquals("New Description", result.getDescription());
            assertEquals(TicketPriority.low, result.getPriority());
            assertEquals(TicketState.in_progress, result.getState());

            // 4 changes: name, description, priority, state
            verify(ticketHistoryRepository, times(4)).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("Throws exception when ticket not found")
        void update_TicketNotFound_ThrowsException() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Name")
                    .description("Desc")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.update(projectId, ticketId, req, "test@example.com"));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws exception when user not found during update")
        void update_UserNotFound_ThrowsException() {
            TicketUpdateRequest req = TicketUpdateRequest.builder()
                    .name("Name")
                    .description("Desc")
                    .type(TicketType.bug)
                    .priority(TicketPriority.high)
                    .state(TicketState.open)
                    .build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.update(projectId, ticketId, req, "unknown@example.com"));
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Ticket Tests")
    class DeleteTests {

        @Test
        @DisplayName("Successfully deletes ticket")
        void delete_Success() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));

            ticketService.delete(projectId, ticketId, "test@example.com");

            verify(ticketRepository).delete(testTicket);
        }

        @Test
        @DisplayName("Delete logs DELETED action in history")
        void delete_LogsHistory() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));

            ticketService.delete(projectId, ticketId, "test@example.com");

            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());

            TicketHistory history = historyCaptor.getValue();
            assertEquals("DELETED", history.getAction());
            assertEquals(ticketId, history.getTicketId());
            assertEquals(userId, history.getAuthorId());
            assertNull(history.getField());
            assertNull(history.getOldValue());
            assertNull(history.getNewValue());
        }

        @Test
        @DisplayName("Throws exception when ticket not found")
        void delete_TicketNotFound_ThrowsException() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.delete(projectId, ticketId, "test@example.com"));
            verify(ticketRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Throws exception when user not found during delete")
        void delete_UserNotFound_ThrowsException() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.delete(projectId, ticketId, "unknown@example.com"));
            verify(ticketRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Comments Tests")
    class CommentsTests {

        private UUID commentId;
        private TicketComment testComment;

        @BeforeEach
        void setUpComments() {
            commentId = UUID.randomUUID();
            testComment = TicketComment.builder()
                    .id(commentId)
                    .ticketId(ticketId)
                    .authorId(userId)
                    .text("Test comment")
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("getComments returns all comments for ticket")
        void getComments_ReturnsComments() {
            TicketComment anotherComment = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(userId)
                    .text("Another comment")
                    .build();
            List<TicketComment> comments = Arrays.asList(testComment, anotherComment);

            when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(comments);

            List<TicketComment> result = ticketService.getComments(ticketId);

            assertEquals(2, result.size());
            assertEquals(testComment, result.get(0));
            verify(ticketCommentRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
        }

        @Test
        @DisplayName("getComments returns empty list when no comments")
        void getComments_ReturnsEmptyList() {
            when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(Collections.emptyList());

            List<TicketComment> result = ticketService.getComments(ticketId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("addComment creates new comment")
        void addComment_Success() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("New comment").build();

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(inv -> {
                TicketComment c = inv.getArgument(0);
                c.setId(UUID.randomUUID());
                return c;
            });

            TicketComment result = ticketService.addComment(ticketId, req, "test@example.com");

            assertEquals(ticketId, result.getTicketId());
            assertEquals(userId, result.getAuthorId());
            assertEquals("New comment", result.getText());
            verify(ticketCommentRepository).save(any(TicketComment.class));
        }

        @Test
        @DisplayName("addComment throws exception when user not found")
        void addComment_UserNotFound_ThrowsException() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("Comment").build();

            when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> ticketService.addComment(ticketId, req, "unknown@example.com"));
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateComment updates existing comment")
        void updateComment_Success() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("Updated text").build();

            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
            when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketComment result = ticketService.updateComment(commentId, req);

            assertEquals("Updated text", result.getText());
            verify(ticketCommentRepository).save(testComment);
        }

        @Test
        @DisplayName("updateComment throws exception when comment not found")
        void updateComment_NotFound_ThrowsException() {
            TicketCommentRequest req = TicketCommentRequest.builder().text("Text").build();

            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.updateComment(commentId, req));
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("deleteComment deletes existing comment")
        void deleteComment_Success() {
            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

            ticketService.deleteComment(commentId);

            verify(ticketCommentRepository).delete(testComment);
        }

        @Test
        @DisplayName("deleteComment throws exception when comment not found")
        void deleteComment_NotFound_ThrowsException() {
            when(ticketCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.deleteComment(commentId));
            verify(ticketCommentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("History Tests")
    class HistoryTests {

        @Test
        @DisplayName("getHistory returns all history entries for ticket")
        void getHistory_ReturnsHistory() {
            TicketHistory history1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(userId)
                    .action("CREATED")
                    .createdAt(OffsetDateTime.now().minusHours(2))
                    .build();
            TicketHistory history2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(userId)
                    .action("UPDATED")
                    .field("name")
                    .oldValue("Old Name")
                    .newValue("New Name")
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(Arrays.asList(history1, history2));

            List<TicketHistory> result = ticketService.getHistory(ticketId);

            assertEquals(2, result.size());
            assertEquals("CREATED", result.get(0).getAction());
            assertEquals("UPDATED", result.get(1).getAction());
            verify(ticketHistoryRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
        }

        @Test
        @DisplayName("getHistory returns empty list when no history")
        void getHistory_ReturnsEmptyList() {
            when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
                    .thenReturn(Collections.emptyList());

            List<TicketHistory> result = ticketService.getHistory(ticketId);

            assertTrue(result.isEmpty());
        }
    }
}