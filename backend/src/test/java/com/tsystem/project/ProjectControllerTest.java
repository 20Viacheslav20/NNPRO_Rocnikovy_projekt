package com.tsystem.project;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.ProjectController;
import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.user.User;
import com.tsystem.service.ProjectService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProjectController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProjectService projectService;

    private User testUser;
    private Project testProject;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        testUser = User.builder().id(UUID.randomUUID()).username("owner@example.com").name("Owner").surname("User").build();
        testProject = Project.builder().id(projectId).name("Test Project").description("Test Description")
                .user(testUser).status(ProjectStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("GET /api/projects - returns projects list")
    void list_ReturnsProjects() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(testProject));
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    @DisplayName("GET /api/projects - empty list")
    void list_Empty() throws Exception {
        when(projectService.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/projects/{id} - returns project")
    void get_Success() throws Exception {
        when(projectService.findById(projectId)).thenReturn(testProject);
        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @DisplayName("GET /api/projects/{id} - 404 if not found")
    void get_NotFound() throws Exception {
        when(projectService.findById(any())).thenThrow(new NotFoundException());
        mockMvc.perform(get("/api/projects/{projectId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/projects/{id} - project update")
    void update_Success() throws Exception {
        Project updated = Project.builder().id(projectId).name("Updated").description("Desc")
                .user(testUser).status(ProjectStatus.ARCHIVED).build();
        when(projectService.update(eq(projectId), any())).thenReturn(updated);
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"description\":\"Desc\",\"status\":\"ARCHIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} - project removal")
    void delete_Success() throws Exception {
        doNothing().when(projectService).delete(projectId);
        mockMvc.perform(delete("/api/projects/{projectId}", projectId))
                .andExpect(status().isNoContent());
        verify(projectService).delete(projectId);
    }
}
