package com.tsystem.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPassword {
    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 6)
    private String newPassword;
}