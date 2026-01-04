package com.tsystem.mapper;

import com.tsystem.model.Project;
import com.tsystem.model.dto.response.ProjectResponse;
import com.tsystem.model.enums.ProjectStatus;
import com.tsystem.model.mapper.ProjectMapper;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    private User testUser;
    private Project testProject;
    private UUID userId;
    private UUID projectId;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        now = OffsetDateTime.now();

        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.USER)
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .status(ProjectStatus.ACTIVE)
                .user(testUser)
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("toResponse - converts all fields correctly")
    void toResponse_ConvertsAllFields() {
        ProjectResponse response = ProjectMapper.toResponse(testProject);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals("Test Project", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    @DisplayName("toResponse - converts owner correctly")
    void toResponse_ConvertsOwner() {
        ProjectResponse response = ProjectMapper.toResponse(testProject);

        assertNotNull(response.getOwner());
        assertEquals(userId, response.getOwner().getId());
        assertEquals("test@example.com", response.getOwner().getUsername());
        assertEquals("Test", response.getOwner().getName());
        assertEquals("User", response.getOwner().getSurname());
    }

    @Test
    @DisplayName("toResponse - handles null description")
    void toResponse_NullDescription() {
        testProject.setDescription(null);

        ProjectResponse response = ProjectMapper.toResponse(testProject);

        assertNull(response.getDescription());
    }

    @Test
    @DisplayName("toResponse - handles all project statuses")
    void toResponse_AllProjectStatuses() {
        for (ProjectStatus status : ProjectStatus.values()) {
            testProject.setStatus(status);

            ProjectResponse response = ProjectMapper.toResponse(testProject);

            assertEquals(status, response.getStatus());
        }
    }

    @Test
    @DisplayName("toResponse - owner has all required fields")
    void toResponse_OwnerHasAllFields() {
        ProjectResponse response = ProjectMapper.toResponse(testProject);

        assertNotNull(response.getOwner().getId());
        assertNotNull(response.getOwner().getUsername());
        assertNotNull(response.getOwner().getName());
        assertNotNull(response.getOwner().getSurname());
    }
}