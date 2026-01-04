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
        controllers = ProjectController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private User testUser;
    private Project testProject;
    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("owner@example.com")
                .email("owner@example.com")
                .name("Owner")
                .surname("User")
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .user(testUser)
                .status(ProjectStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/projects")
    class ListProjectsTests {

        @Test
        @DisplayName("returns projects list")
        void list_ReturnsProjects() throws Exception {
            when(projectService.findAll()).thenReturn(List.of(testProject));

            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(projectId.toString()))
                    .andExpect(jsonPath("$[0].name").value("Test Project"))
                    .andExpect(jsonPath("$[0].description").value("Test Description"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$[0].owner.username").value("owner@example.com"));

            verify(projectService).findAll();
        }

        @Test
        @DisplayName("returns empty list when no projects")
        void list_Empty() throws Exception {
            when(projectService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("returns multiple projects")
        void list_MultipleProjects() throws Exception {
            Project project2 = Project.builder()
                    .id(UUID.randomUUID())
                    .name("Project 2")
                    .description("Description 2")
                    .user(testUser)
                    .status(ProjectStatus.ARCHIVED)
                    .build();

            when(projectService.findAll()).thenReturn(Arrays.asList(testProject, project2));

            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Test Project"))
                    .andExpect(jsonPath("$[1].name").value("Project 2"));
        }
    }

    @Nested
    @DisplayName("POST /api/projects")
    class CreateProjectTests {

        @Test
        @DisplayName("creates project successfully")
        @WithMockUser(username = "owner@example.com")
        void create_Success() throws Exception {
            when(projectService.create(any(), eq("owner@example.com"))).thenReturn(testProject);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test Project",
                                        "description": "Test Description"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(projectId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.owner.username").value("owner@example.com"));

            verify(projectService).create(any(), eq("owner@example.com"));
        }

        @Test
        @DisplayName("creates project with minimal data")
        @WithMockUser(username = "owner@example.com")
        void create_MinimalData() throws Exception {
            Project minimalProject = Project.builder()
                    .id(projectId)
                    .name("Minimal Project")
                    .user(testUser)
                    .status(ProjectStatus.ACTIVE)
                    .build();

            when(projectService.create(any(), eq("owner@example.com"))).thenReturn(minimalProject);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Minimal Project"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Minimal Project"));
        }

        @Test
        @DisplayName("returns 400 when name is missing")
        @WithMockUser(username = "owner@example.com")
        void create_MissingName_Returns400() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "description": "Only description"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).create(any(), any());
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        @WithMockUser(username = "owner@example.com")
        void create_BlankName_Returns400() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "   ",
                                        "description": "Some description"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).create(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/projects/{projectId}")
    class GetProjectTests {

        @Test
        @DisplayName("returns project by id")
        void get_Success() throws Exception {
            when(projectService.findById(projectId)).thenReturn(testProject);

            mockMvc.perform(get("/api/projects/{projectId}", projectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.owner.id").value(userId.toString()));

            verify(projectService).findById(projectId);
        }

        @Test
        @DisplayName("returns 404 when project not found")
        void get_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            when(projectService.findById(randomId)).thenThrow(new NotFoundException("Project not found"));

            mockMvc.perform(get("/api/projects/{projectId}", randomId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns project with null description")
        void get_NullDescription() throws Exception {
            testProject.setDescription(null);
            when(projectService.findById(projectId)).thenReturn(testProject);

            mockMvc.perform(get("/api/projects/{projectId}", projectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").doesNotExist());
        }
    }

    @Nested
    @DisplayName("PUT /api/projects/{projectId}")
    class UpdateProjectTests {

        @Test
        @DisplayName("updates project successfully")
        void update_Success() throws Exception {
            Project updated = Project.builder()
                    .id(projectId)
                    .name("Updated Project")
                    .description("Updated Description")
                    .user(testUser)
                    .status(ProjectStatus.ARCHIVED)
                    .build();

            when(projectService.update(eq(projectId), any())).thenReturn(updated);

            mockMvc.perform(put("/api/projects/{projectId}", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Updated Project",
                                        "description": "Updated Description",
                                        "status": "ARCHIVED"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Project"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));

            verify(projectService).update(eq(projectId), any());
        }

        @Test
        @DisplayName("updates only name")
        void update_OnlyName() throws Exception {
            Project updated = Project.builder()
                    .id(projectId)
                    .name("New Name")
                    .description("Test Description")
                    .user(testUser)
                    .status(ProjectStatus.ACTIVE)
                    .build();

            when(projectService.update(eq(projectId), any())).thenReturn(updated);

            mockMvc.perform(put("/api/projects/{projectId}", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "New Name",
                                        "description": "Test Description",
                                        "status": "ACTIVE"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Name"));
        }

        @Test
        @DisplayName("returns 404 when project not found")
        void update_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            when(projectService.update(eq(randomId), any()))
                    .thenThrow(new NotFoundException("Project not found"));

            mockMvc.perform(put("/api/projects/{projectId}", randomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Updated",
                                        "description": "Desc",
                                        "status": "ACTIVE"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when name is missing")
        void update_MissingName_Returns400() throws Exception {
            mockMvc.perform(put("/api/projects/{projectId}", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "description": "Only description",
                                        "status": "ACTIVE"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).update(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/projects/{projectId}")
    class DeleteProjectTests {

        @Test
        @DisplayName("deletes project successfully")
        @WithMockUser(username = "owner@example.com")
        void delete_Success() throws Exception {
            doNothing().when(projectService).delete(projectId);

            mockMvc.perform(delete("/api/projects/{projectId}", projectId))
                    .andExpect(status().isNoContent());

            verify(projectService).delete(projectId);
        }

        @Test
        @DisplayName("returns 404 when project not found")
        @WithMockUser(username = "owner@example.com")
        void delete_NotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            doThrow(new NotFoundException("Project not found"))
                    .when(projectService).delete(randomId);

            mockMvc.perform(delete("/api/projects/{projectId}", randomId))
                    .andExpect(status().isNotFound());
        }
    }
}