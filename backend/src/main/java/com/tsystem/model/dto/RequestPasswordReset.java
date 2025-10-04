package com.tsystem.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPasswordReset {
    @NotBlank
    private String login;   // username или email
}