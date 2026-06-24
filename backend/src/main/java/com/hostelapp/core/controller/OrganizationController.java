package com.hostelapp.core.controller;

import com.hostelapp.core.dto.OrganizationOnboardRequest;
import com.hostelapp.core.entity.Workspace;
import com.hostelapp.core.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/onboard")
    public ResponseEntity<Workspace> onboard(@Valid @RequestBody OrganizationOnboardRequest request) {
        return ResponseEntity.ok(organizationService.onboard(request));
    }
}
