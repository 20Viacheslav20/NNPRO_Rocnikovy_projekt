package com.tsystem.model.user;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public enum SystemPermission {

    // Ticket permissions
    TICKET_CREATE("ticket:create"),
    TICKET_READ_ALL("ticket:read_all"),
    TICKET_READ_ASSIGNED("ticket:read_assigned"),
    TICKET_UPDATE("ticket:update"),
    TICKET_UPDATE_ASSIGNED("ticket:update_assigned"),
    TICKET_DELETE("ticket:delete"),

    // Project permissions
    PROJECT_CREATE("project:create"),
    PROJECT_READ_ALL("project:read_all"),
    //PROJECT_READ_OWN("project:read_own"),
    PROJECT_UPDATE("project:update"),
    PROJECT_DELETE("project:delete"),

    // User permissions
    USER_READ_ALL("user:read_all"),
    USER_UPDATE_SELF("user:update_self"),
    USER_UPDATE_ROLE("user:update_role"),
    USER_DELETE("user:delete"),

    // System-level
    SYSTEM_AUDIT_READ("system:audit_read"),
    SYSTEM_ADMIN_ACTIONS("system:admin_actions");

    @Getter
    private final String permission;

    public static Set<SystemPermission> all() {
        return Set.of(values());
    }
}