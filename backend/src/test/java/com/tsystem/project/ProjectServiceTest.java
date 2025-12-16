package com.tsystem.project;

import com.tsystem.model.*;
import com.tsystem.model.dto.request.ProjectCreateRequest;
import com.tsystem.model.user.User;
import com.tsystem.repository.*;
import com.tsystem.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProjectService projectService;

    @Test
    void create_project_success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("u@test.com")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProjectCreateRequest req = new ProjectCreateRequest("Test", "Desc");

        Project p = projectService.create(req, "u@test.com");

        assertEquals("Test", p.getName());
        assertEquals(user, p.getUser());
    }
}