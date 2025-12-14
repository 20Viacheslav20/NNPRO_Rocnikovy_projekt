package com.tsystem.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    @NotBlank
    @Size(min = 1, max = 120)
    private String surname;

    @Size(min = 6, max = 200)
    private String password;

    @NotNull
    private String role; // "ADMIN", "USER", "PROJECT_MANAGER"
}