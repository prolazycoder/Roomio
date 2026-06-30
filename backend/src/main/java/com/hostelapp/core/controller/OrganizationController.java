package com.hostelapp.core.controller;

import com.hostelapp.core.dto.OrganizationOnboardRequest;
import com.hostelapp.core.entity.Workspace;
import com.hostelapp.core.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization and workspace provisioning — public onboarding endpoint")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/onboard")
    @Operation(
            summary = "Onboard a new organization",
            description = "Creates an organization and its first workspace (tenant). This endpoint is public — no auth required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization and workspace created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Workspace> onboard(@Valid @RequestBody OrganizationOnboardRequest request) {
        return ResponseEntity.ok(organizationService.onboard(request));
    }
}

