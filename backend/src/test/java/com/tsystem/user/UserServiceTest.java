package com.tsystem.user;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.dto.request.UserRequest;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void create_user_success() {
        UserRequest req = UserRequest.builder()
                .email("u@test.com")
                .name("U")
                .surname("S")
                .password("123456")
                .role("USER")
                .build();

        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User user = userService.create(req);

        assertEquals("u@test.com", user.getEmail());
        assertEquals(SystemRole.USER, user.getRole());
        assertEquals("hash", user.getPassword());
    }

    @Test
    void find_user_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.findById(id));
    }
}

