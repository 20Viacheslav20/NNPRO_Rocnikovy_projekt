package com.tsystem.ticket;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.TicketController;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.user.User;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TicketController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TicketService ticketService;

    @Test
    void list_tickets_ok() throws Exception {
        UUID projectId = UUID.randomUUID();

        User author = User.builder()
                .id(UUID.randomUUID())
                .username("author@test.com")
                .name("Author")
                .surname("User")
                .build();

        Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .name("Bug")
                .author(author)
                .project(Project.builder().id(projectId).build())
                .build();

        when(ticketService.getAllByProjectId(projectId))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/projects/{id}/tickets", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bug"))
                .andExpect(jsonPath("$[0].owner.username").value("author@test.com"));
    }
}

