package com.hostelapp.core.entity;

public enum Role {
    OWNER,
    MANAGER,
    TENANT;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
