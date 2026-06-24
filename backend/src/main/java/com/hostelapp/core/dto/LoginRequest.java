package com.hostelapp.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotNull
    private Long workspaceId;

    @Email
    private String email;

    private String password;
}
