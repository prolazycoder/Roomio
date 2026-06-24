package com.hostelapp.core.config;

/**
 * Interface to be implemented by authentication principals that carry a workspace context.
 */
public interface TenantPrincipal {
    Long getWorkspaceId();
}
