package com.tsystem.project;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.dto.request.ProjectCreateRequest;
import com.tsystem.model.dto.request.ProjectUpdateRequest;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.user.User;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProjectService projectService;

    private User testUser;
    private Project testProject;
    private UUID userId, projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        testUser = User.builder().id(userId).email("owner@example.com").username("owner@example.com")
                .name("Owner").surname("User").build();

        testProject = Project.builder().id(projectId).name("Test Project").description("Test Description")
                .user(testUser).status(ProjectStatus.ACTIVE).build();
    }

    @Nested
    @DisplayName("Create Project Tests")
    class CreateTests {
        @Test
        @DisplayName("Successful project creation")
        void create_Success() {
            ProjectCreateRequest req = new ProjectCreateRequest("New Project", "New Description");

            when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

            Project result = projectService.create(req, "owner@example.com");

            assertEquals("New Project", result.getName());
            assertEquals(testUser, result.getUser());
            verify(projectRepository).save(any(Project.class));
        }

        @Test
        @DisplayName("Project creation - user not found")
        void create_UserNotFound() {
            ProjectCreateRequest req = new ProjectCreateRequest("Project", "Description");
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> projectService.create(req, "nonexistent@example.com"));
        }
    }

    @Nested
    @DisplayName("Find All Projects Tests")
    class FindAllTests {
        @Test
        @DisplayName("Returns all projects list")
        void findAll_ReturnsProjects() {
            Project project2 = Project.builder().id(UUID.randomUUID()).name("Project 2").user(testUser).build();
            when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject, project2));

            List<Project> result = projectService.findAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Returns empty list")
        void findAll_Empty() {
            when(projectRepository.findAll()).thenReturn(Collections.emptyList());
            assertTrue(projectService.findAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Successfully finds project")
        void findById_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            assertEquals(testProject, projectService.findById(projectId));
        }

        @Test
        @DisplayName("Throws exception if not found")
        void findById_NotFound() {
            when(projectRepository.findById(any())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> projectService.findById(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("Update Project Tests")
    class UpdateTests {
        @Test
        @DisplayName("Successful project update")
        void update_Success() {
            ProjectUpdateRequest req = ProjectUpdateRequest.builder()
                    .name("Updated Name").description("Updated Desc").status(ProjectStatus.ARCHIVED).build();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
            when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

            Project result = projectService.update(projectId, req);

            assertEquals("Updated Name", result.getName());
            assertEquals(ProjectStatus.ARCHIVED, result.getStatus());
        }

        @Test
        @DisplayName("Update non-existing project")
        void update_NotFound() {
            ProjectUpdateRequest req = ProjectUpdateRequest.builder()
                    .name("Name").description("Desc").status(ProjectStatus.ACTIVE).build();
            when(projectRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> projectService.update(UUID.randomUUID(), req));
        }
    }

    @Nested
    @DisplayName("Delete Project Tests")
    class DeleteTests {
        @Test
        @DisplayName("Successful project removal")
        void delete_Success() {
            doNothing().when(projectRepository).deleteById(projectId);
            projectService.delete(projectId);
            verify(projectRepository).deleteById(projectId);
        }
    }
}