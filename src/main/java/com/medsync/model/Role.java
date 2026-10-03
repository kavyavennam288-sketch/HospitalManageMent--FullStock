package com.medsync.model;

public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN;

    // Explicitly define this even though Java provides it — prevents
    // any ambiguity when the enum is used via a generic type or proxy.
    public String getRoleName() {
        return this.name();
    }
}