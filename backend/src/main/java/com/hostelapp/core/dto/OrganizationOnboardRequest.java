package com.hostelapp.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationOnboardRequest {
    @NotBlank
    private String organizationName;

    @NotBlank
    private String workspaceName;

    private String subdomain;

    @NotBlank
    @Email
    private String adminEmail;

    @NotBlank
    private String adminPassword;

    private String govIdType;
    private String govIdNumber;
}
