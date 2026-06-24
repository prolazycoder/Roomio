package com.hostelapp.core.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Filter to extract tenant information from the request header or JWT security context.
 */
@Component
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Workspace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantHeader = httpRequest.getHeader(TENANT_HEADER);

        Long tenantId = null;

        // 1. First strategy: Check Custom Header
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            try {
                tenantId = Long.valueOf(tenantHeader);
            } catch (NumberFormatException e) {
                // Invalid format, fall through to JWT
            }
        }

        // 2. Second strategy: Fallback to authenticated user JWT claims
        if (tenantId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof TenantPrincipal principal) {
                tenantId = principal.getWorkspaceId();
            }
        }

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
