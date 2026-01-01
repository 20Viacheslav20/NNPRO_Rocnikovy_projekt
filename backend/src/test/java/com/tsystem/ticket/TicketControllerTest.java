package com.tsystem.ticket;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.TicketController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.Ticket;
import com.tsystem.model.TicketComment;
import com.tsystem.model.TicketHistory;
import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import com.tsystem.model.enums.TicketType;
import com.tsystem.model.user.User;
import com.tsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
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

    private UUID projectId, ticketId, commentId;
    private User author;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        author = User.builder().id(UUID.randomUUID()).username("author@test.com").name("Author").surname("User").build();

        testTicket = Ticket.builder()
                .id(ticketId).name("Test Bug").description("Bug description")
                .type(TicketType.bug).priority(TicketPriority.high).state(TicketState.open)
                .author(author).project(Project.builder().id(projectId).build()).build();
    }

    @Nested
    @DisplayName("Ticket CRUD Tests")
    class TicketCrudTests {

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
        @DisplayName("GET /api/projects/{pid}/tickets/{tid} - get single ticket")
        void get_Success() throws Exception {
            when(ticketService.get(projectId, ticketId)).thenReturn(testTicket);

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Test Bug"))
                    .andExpect(jsonPath("$.type").value("bug"));
        }

        @Test
        @DisplayName("GET /api/projects/{pid}/tickets/{tid} - not found")
        void get_NotFound() throws Exception {
            when(ticketService.get(any(), any())).thenThrow(new NotFoundException("Ticket not found"));

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}", projectId, ticketId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/projects/{id}/tickets - create ticket")
        @WithMockUser(username = "author@test.com")
        void create_Success() throws Exception {
            when(ticketService.create(eq(projectId), any(), eq("author@test.com"))).thenReturn(testTicket);

            mockMvc.perform(post("/api/projects/{id}/tickets", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test Bug\",\"type\":\"bug\",\"priority\":\"high\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Test Bug"));
        }

        @Test
        @DisplayName("PUT /api/projects/{pid}/tickets/{tid} - ticket update")
        @WithMockUser(username = "author@test.com")
        void update_Success() throws Exception {
            Ticket updated = Ticket.builder()
                    .id(ticketId).name("Updated Bug").description("New desc")
                    .type(TicketType.bug).priority(TicketPriority.low).state(TicketState.in_progress)
                    .author(author).project(Project.builder().id(projectId).build()).build();

            when(ticketService.update(eq(projectId), eq(ticketId), any(), eq("author@test.com"))).thenReturn(updated);

            mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}", projectId, ticketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Updated Bug\",\"description\":\"New desc\",\"type\":\"bug\",\"priority\":\"low\",\"state\":\"in_progress\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Bug"))
                    .andExpect(jsonPath("$.state").value("in_progress"));
        }

        @Test
        @DisplayName("PUT /api/projects/{pid}/tickets/{tid} - 404 if not found")
        @WithMockUser(username = "author@test.com")
        void update_NotFound() throws Exception {
            when(ticketService.update(any(), any(), any(), any())).thenThrow(new NotFoundException("Ticket not found"));

            mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}", projectId, ticketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"X\",\"type\":\"bug\",\"priority\":\"low\",\"state\":\"open\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/projects/{pid}/tickets/{tid} - ticket removal")
        @WithMockUser(username = "author@test.com")
        void delete_Success() throws Exception {
            doNothing().when(ticketService).delete(projectId, ticketId, "author@test.com");

            mockMvc.perform(delete("/api/projects/{pid}/tickets/{tid}", projectId, ticketId))
                    .andExpect(status().isNoContent());

            verify(ticketService).delete(projectId, ticketId, "author@test.com");
        }

        @Test
        @DisplayName("List with multiple tickets")
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

    @Nested
    @DisplayName("Comments Tests")
    class CommentsTests {

        private TicketComment testComment;

        @BeforeEach
        void setUpComment() {
            testComment = TicketComment.builder()
                    .id(commentId)
                    .ticketId(ticketId)
                    .authorId(author.getId())
                    .text("Test comment")
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("GET /api/projects/{pid}/tickets/{tid}/comments - list comments")
        void listComments_Success() throws Exception {
            when(ticketService.getComments(ticketId)).thenReturn(List.of(testComment));

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}/comments", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].text").value("Test comment"));
        }

        @Test
        @DisplayName("GET /api/projects/{pid}/tickets/{tid}/comments - empty list")
        void listComments_Empty() throws Exception {
            when(ticketService.getComments(ticketId)).thenReturn(List.of());

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}/comments", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("POST /api/projects/{pid}/tickets/{tid}/comments - add comment")
        @WithMockUser(username = "author@test.com")
        void addComment_Success() throws Exception {
            when(ticketService.addComment(eq(ticketId), any(), eq("author@test.com"))).thenReturn(testComment);

            mockMvc.perform(post("/api/projects/{pid}/tickets/{tid}/comments", projectId, ticketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"Test comment\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.text").value("Test comment"));
        }

        @Test
        @DisplayName("PUT /api/projects/{pid}/tickets/{tid}/comments/{cid} - update comment")
        void updateComment_Success() throws Exception {
            TicketComment updated = TicketComment.builder()
                    .id(commentId)
                    .ticketId(ticketId)
                    .authorId(author.getId())
                    .text("Updated comment")
                    .build();

            when(ticketService.updateComment(eq(commentId), any())).thenReturn(updated);

            mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}/comments/{cid}", projectId, ticketId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"Updated comment\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text").value("Updated comment"));
        }

        @Test
        @DisplayName("PUT - comment not found")
        void updateComment_NotFound() throws Exception {
            when(ticketService.updateComment(any(), any())).thenThrow(new NotFoundException("Comment not found"));

            mockMvc.perform(put("/api/projects/{pid}/tickets/{tid}/comments/{cid}", projectId, ticketId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"Updated\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/projects/{pid}/tickets/{tid}/comments/{cid} - delete comment")
        void deleteComment_Success() throws Exception {
            doNothing().when(ticketService).deleteComment(commentId);

            mockMvc.perform(delete("/api/projects/{pid}/tickets/{tid}/comments/{cid}", projectId, ticketId, commentId))
                    .andExpect(status().isNoContent());

            verify(ticketService).deleteComment(commentId);
        }

        @Test
        @DisplayName("DELETE - comment not found")
        void deleteComment_NotFound() throws Exception {
            doThrow(new NotFoundException("Comment not found")).when(ticketService).deleteComment(commentId);

            mockMvc.perform(delete("/api/projects/{pid}/tickets/{tid}/comments/{cid}", projectId, ticketId, commentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("History Tests")
    class HistoryTests {

        @Test
        @DisplayName("GET /api/projects/{pid}/tickets/{tid}/history - get history")
        void getHistory_Success() throws Exception {
            TicketHistory history = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author.getId())
                    .action("CREATED")
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(ticketService.getHistory(ticketId)).thenReturn(List.of(history));

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}/history", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].action").value("CREATED"));
        }

        @Test
        @DisplayName("GET history - empty")
        void getHistory_Empty() throws Exception {
            when(ticketService.getHistory(ticketId)).thenReturn(List.of());

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}/history", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET history - with field changes")
        void getHistory_WithFieldChanges() throws Exception {
            TicketHistory history = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author.getId())
                    .action("UPDATED")
                    .field("state")
                    .oldValue("open")
                    .newValue("in_progress")
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(ticketService.getHistory(ticketId)).thenReturn(List.of(history));

            mockMvc.perform(get("/api/projects/{pid}/tickets/{tid}/history", projectId, ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].action").value("UPDATED"))
                    .andExpect(jsonPath("$[0].field").value("state"))
                    .andExpect(jsonPath("$[0].oldValue").value("open"))
                    .andExpect(jsonPath("$[0].newValue").value("in_progress"));
        }
    }
}