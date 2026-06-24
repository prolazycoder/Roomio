package com.hostelapp.core.entity;

import com.hostelapp.core.config.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "workspaceId", type = Long.class))
@Filter(name = "tenantFilter", condition = "workspace_id = :workspaceId")
public abstract class BaseTenantEntity {

    @Column(name = "workspace_id", nullable = false, updatable = false)
    private Long workspaceId;

    @PrePersist
    public void prePersist() {
        if (this.workspaceId == null) {
            Long currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                this.workspaceId = currentTenant;
            } else {
                throw new IllegalStateException("No tenant ID found in TenantContext during persist!");
            }
        }
    }
}
