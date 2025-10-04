package com.tsystem.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 60)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    @NotBlank
    @Size(min = 1, max = 120)
    private String surname;

    @NotBlank
    @Size(min = 6, max = 200)
    private String password;
}