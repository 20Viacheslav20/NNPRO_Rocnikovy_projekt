package com.tsystem.ticket;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.dto.request.TicketUpdateRequest;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.TicketRepository;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Ticket result = ticketService.create(projectId, req, "admin@test.com");

            assertEquals("New Bug", result.getName());
            assertEquals(adminUser, result.getAuthor());
        }

        @Test
        @DisplayName("Creation of ticket with assignee")
        void create_WithAssignee() {
            TicketCreateRequest req = TicketCreateRequest.builder()
                    .name("Task").type(TicketType.task).priority(TicketPriority.med)
                    .assigneeId(regularUser.getId()).build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Ticket result = ticketService.create(projectId, req, "admin@test.com");

            assertEquals(regularUser, result.getAssignee());
        }

        @Test
        @DisplayName("Ticket creation - project not found")
        void create_ProjectNotFound() {
            TicketCreateRequest req = TicketCreateRequest.builder().name("Bug").type(TicketType.bug).priority(TicketPriority.low).build();
            when(projectRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> ticketService.create(projectId, req, "admin@test.com"));
        }
    }

    @Nested
    @DisplayName("Get Ticket Tests")
    class GetTests {
        @Test
        @DisplayName("Admin can view any ticket")
        void get_AsAdmin() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.of(adminUser));

            Ticket result = ticketService.get(projectId, ticketId, "admin@test.com");
            assertEquals(testTicket, result);
        }

        @Test
        @DisplayName("Project Manager can view any ticket")
        void get_AsProjectManager() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("manager@test.com")).thenReturn(Optional.of(managerUser));

            Ticket result = ticketService.get(projectId, ticketId, "manager@test.com");
            assertEquals(testTicket, result);
        }

        @Test
        @DisplayName("User can view assigned ticket")
        void get_AsAssignedUser() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(regularUser));

            Ticket result = ticketService.get(projectId, ticketId, "user@test.com");
            assertEquals(testTicket, result);
        }

        @Test
        @DisplayName("User cannot view unassigned ticket")
        void get_NotAssigned_Forbidden() {
            Ticket unassignedTicket = Ticket.builder().id(ticketId).project(testProject).assignee(null).build();
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(unassignedTicket));
            when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(regularUser));

            assertThrows(AccessDeniedException.class, () -> ticketService.get(projectId, ticketId, "user@test.com"));
        }

        @Test
        @DisplayName("Ticket not found")
        void get_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> ticketService.get(projectId, ticketId, "admin@test.com"));
        }
    }

    @Nested
    @DisplayName("Get All By Project Tests")
    class GetAllByProjectTests {
        @Test
        @DisplayName("Returns all tickets to project")
        void getAllByProjectId_Success() {
            Ticket ticket2 = Ticket.builder().id(UUID.randomUUID()).name("Ticket 2").project(testProject).build();
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(ticketRepository.findByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(Arrays.asList(testTicket, ticket2));

            List<Ticket> result = ticketService.getAllByProjectId(projectId);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Find By Assignee Tests")
    class FindByAssigneeTests {
        @Test
        @DisplayName("Returns assigned to user tickets")
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
                    .name("Updated").description("New desc").type(TicketType.task)
                    .priority(TicketPriority.low).state(TicketState.in_progress).build();

            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Ticket result = ticketService.update(projectId, ticketId, req);

            assertEquals("Updated", result.getName());
            assertEquals(TicketState.in_progress, result.getState());
        }

        @Test
        @DisplayName("Update non-existing ticket")
        void update_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());
            TicketUpdateRequest req = TicketUpdateRequest.builder().name("X").build();
            assertThrows(NotFoundException.class, () -> ticketService.update(projectId, ticketId, req));
        }
    }

    @Nested
    @DisplayName("Delete Ticket Tests")
    class DeleteTests {
        @Test
        @DisplayName("Successful ticket removal")
        void delete_Success() {
            when(ticketRepository.findByIdAndProjectId(ticketId, projectId)).thenReturn(Optional.of(testTicket));
            doNothing().when(ticketRepository).delete(testTicket);

            ticketService.delete(projectId, ticketId);

            verify(ticketRepository).delete(testTicket);
        }

        @Test
        @DisplayName("Non-existing ticket removal")
        void delete_NotFound() {
            when(ticketRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> ticketService.delete(projectId, ticketId));
        }
    }
}