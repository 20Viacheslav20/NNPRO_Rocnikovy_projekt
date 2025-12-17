package com.tsystem.ticket;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.TicketController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
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
        controllers = TicketController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TicketService ticketService;

    private UUID projectId, ticketId;
    private User author;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        author = User.builder().id(UUID.randomUUID()).username("author@test.com").name("Author").surname("User").build();

        testTicket = Ticket.builder()
                .id(ticketId).name("Test Bug").description("Bug description")
                .type(TicketType.bug).priority(TicketPriority.high).state(TicketState.open)
                .author(author).project(Project.builder().id(projectId).build()).build();
    }

    @Test
    @DisplayName("GET /api/projects/{id}/tickets - returns ticket list")
    void list_ReturnsTickets() throws Exception {
        when(ticketService.getAllByProjectId(projectId)).thenReturn(List.of(testTicket));

        mockMvc.perform(get("/api/projects/{id}/tickets", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Bug"))
                .andExpect(jsonPath("$[0].type").value("bug"));
    }

    @Test
    @DisplayName("GET /api/projects/{id}/tickets - empty list")
    void list_Empty() throws Exception {
        when(ticketService.getAllByProjectId(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects/{id}/tickets", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/projects/{pid}/tickets/{tid} - ticket update")
    void update_Success() throws Exception {
        Ticket updated = Ticket.builder()
                .id(ticketId).name("Updated Bug").description("New desc")
                .type(TicketType.bug).priority(TicketPriority.low).state(TicketState.in_progress)
                .author(author).project(Project.builder().id(projectId).build()).build();

        when(ticketService.update(eq(projectId), eq(ticketId), any())).thenReturn(updated);

        mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}", projectId, ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Bug\",\"description\":\"New desc\",\"type\":\"bug\",\"priority\":\"low\",\"state\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Bug"))
                .andExpect(jsonPath("$.state").value("in_progress"));
    }

    @Test
    @DisplayName("PUT /api/projects/{pid}/tickets/{tid} - 404 if not found")
    void update_NotFound() throws Exception {
        when(ticketService.update(any(), any(), any())).thenThrow(new NotFoundException("Ticket not found"));

        mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}", projectId, ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"type\":\"bug\",\"priority\":\"low\",\"state\":\"open\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/projects/{pid}/tickets/{tid} - ticket removal")
    void delete_Success() throws Exception {
        doNothing().when(ticketService).delete(projectId, ticketId);

        mockMvc.perform(delete("/api/projects/{pid}/tickets/{tid}", projectId, ticketId))
                .andExpect(status().isNoContent());

        verify(ticketService).delete(projectId, ticketId);
    }

    @Test
    @DisplayName("List with few tickets")
    void list_MultipleTickets() throws Exception {
        Ticket ticket2 = Ticket.builder()
                .id(UUID.randomUUID()).name("Task").type(TicketType.task).priority(TicketPriority.med)
                .author(author).project(Project.builder().id(projectId).build()).build();

        when(ticketService.getAllByProjectId(projectId)).thenReturn(Arrays.asList(testTicket, ticket2));

        mockMvc.perform(get("/api/projects/{id}/tickets", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}