package com.tsystem.user;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.UserController;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.UserService;
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
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserService userService;

    @Test
    void get_all_users_ok() throws Exception {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("u@test.com")
                .name("U")
                .surname("S")
                .role(SystemRole.USER)
                .build();

        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u@test.com"));
    }
}

