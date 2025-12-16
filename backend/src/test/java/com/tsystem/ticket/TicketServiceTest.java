package com.tsystem.ticket;

import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.dto.request.TicketCreateRequest;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.TicketRepository;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    TicketService ticketService;

    @Test
    void create_ticket_success() {
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .build();

        User author = User.builder()
                .id(UUID.randomUUID())
                .username("u@test.com")
                .role(SystemRole.ADMIN)
                .build();

        TicketCreateRequest req = TicketCreateRequest.builder()
                .name("Bug #1")
                .type(TicketType.bug)
                .priority(TicketPriority.high)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("u@test.com")).thenReturn(Optional.of(author));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Ticket t = ticketService.create(projectId, req, "u@test.com");

        assertEquals("Bug #1", t.getName());
        assertEquals(author, t.getAuthor());
        assertEquals(project, t.getProject());
    }

    @Test
    void get_ticket_user_not_assigned_forbidden() {
        UUID projectId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user@test.com")
                .role(SystemRole.USER)
                .build();

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .project(Project.builder().id(projectId).build())
                .assignee(null)
                .build();

        when(ticketRepository.findByIdAndProjectId(ticketId, projectId))
                .thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("user@test.com"))
                .thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class,
                () -> ticketService.get(projectId, ticketId, "user@test.com"));
    }
}

