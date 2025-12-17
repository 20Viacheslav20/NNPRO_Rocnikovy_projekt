package com.tsystem.ticket;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.TicketAssigneeController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.user.User;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TicketAssigneeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TicketAssigneeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TicketService ticketService;

    private UUID userId;
    private User assignee;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        assignee = User.builder().id(userId).username("user@test.com").name("Test").surname("User").build();
        testTicket = Ticket.builder()
                .id(UUID.randomUUID()).name("Assigned Ticket").description("Test")
                .type(TicketType.task).priority(TicketPriority.med)
                .author(assignee).assignee(assignee)
                .project(Project.builder().id(UUID.randomUUID()).build()).build();
    }

    @Test @DisplayName("GET /api/tickets/assignee/{userId} - returns tickets")
    void getByAssignee_Success() throws Exception {
        when(ticketService.findByAssignee(userId)).thenReturn(List.of(testTicket));
        mockMvc.perform(get("/api/tickets/assignee/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Assigned Ticket"));
    }

    @Test @DisplayName("GET /api/tickets/assignee/{userId} - empty list")
    void getByAssignee_Empty() throws Exception {
        when(ticketService.findByAssignee(userId)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/tickets/assignee/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test @DisplayName("GET /api/tickets/assignee/{userId} - a few tickets")
    void getByAssignee_MultipleTickets() throws Exception {
        Ticket ticket2 = Ticket.builder()
                .id(UUID.randomUUID()).name("Another Ticket").type(TicketType.bug).priority(TicketPriority.high)
                .author(assignee).assignee(assignee).project(Project.builder().id(UUID.randomUUID()).build()).build();
        when(ticketService.findByAssignee(userId)).thenReturn(Arrays.asList(testTicket, ticket2));
        mockMvc.perform(get("/api/tickets/assignee/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test @DisplayName("GET /api/tickets/assignee/{userId} - 404")
    void getByAssignee_UserNotFound() throws Exception {
        when(ticketService.findByAssignee(any())).thenThrow(new NotFoundException("User not found"));
        mockMvc.perform(get("/api/tickets/assignee/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
