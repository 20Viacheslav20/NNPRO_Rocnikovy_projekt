package com.tsystem.project;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.ProjectController;
import com.tsystem.model.Project;
import com.tsystem.model.user.User;
import com.tsystem.service.ProjectService;
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
        controllers = ProjectController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProjectService projectService;

    @Test
    void list_projects_ok() throws Exception {

        User owner = User.builder()
                .id(UUID.randomUUID())
                .username("owner@test.com")
                .name("Owner")
                .surname("User")
                .build();

        Project project = Project.builder()
                .id(UUID.randomUUID())
                .name("P1")
                .user(owner) // ← 🔥 ВАЖЛИВО
                .build();

        when(projectService.findAll()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("P1"))
                .andExpect(jsonPath("$[0].owner.username").value("owner@test.com"));
    }
}

