package com.hostelapp.core.service;

import com.hostelapp.core.entity.Workspace;
import com.hostelapp.core.dto.OrganizationOnboardRequest;

public interface OrganizationService {
    Workspace onboard(OrganizationOnboardRequest request);
}
