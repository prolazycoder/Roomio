package com.hostelapp.core.service.impl;

import com.hostelapp.core.dto.OrganizationOnboardRequest;
import com.hostelapp.core.entity.Organization;
import com.hostelapp.core.entity.Role;
import com.hostelapp.core.entity.User;
import com.hostelapp.core.entity.Workspace;
import com.hostelapp.core.repository.OrganizationRepository;
import com.hostelapp.core.repository.UserRepository;
import com.hostelapp.core.repository.WorkspaceRepository;
import com.hostelapp.core.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Workspace onboard(OrganizationOnboardRequest request) {
        // 1. Create Organization
        Organization organization = new Organization();
        organization.setName(request.getOrganizationName());
        organization = organizationRepository.save(organization);

        // 2. Create Workspace
        Workspace workspace = new Workspace();
        workspace.setOrganization(organization);
        workspace.setName(request.getWorkspaceName());
        workspace.setSubdomain(request.getSubdomain() != null ? request.getSubdomain().toLowerCase() : null);
        workspace = workspaceRepository.save(workspace);

        // 3. Register Administrator (OWNER role)
        User admin = new User();
        admin.setEmail(request.getAdminEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        admin.setRole(Role.OWNER);
        admin.setGovIdType(request.getGovIdType());
        admin.setGovIdNumberMasked(maskGovId(request.getGovIdNumber()));
        
        // Explicitly set the workspace ID because there's no active TenantContext yet during onboarding.
        admin.setWorkspaceId(workspace.getId());
        userRepository.save(admin);

        return workspace;
    }

    private String maskGovId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return null;
        }
        String clean = rawId.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        return "*".repeat(clean.length() - 4) + clean.substring(clean.length() - 4);
    }
}
