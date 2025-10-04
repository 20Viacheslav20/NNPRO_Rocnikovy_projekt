package com.tsystem.model.user;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public enum SystemPermission {
    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete");


    @Getter
    private final String permission;

    public static Set<SystemPermission> getAllPermissions() {
        return Set.of(SystemPermission.values());
    }

    public static Set<SystemPermission> getUserPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(USER_CREATE);
        permissions.add(USER_READ);
        permissions.add(USER_UPDATE);
        permissions.add(USER_DELETE);

        return permissions;
    }

    public static Set<SystemPermission> getAdminPermissions() {
//        Set<SystemPermission> permissions = new HashSet<>();
//        permissions.add(P_CREATE);
//        permissions.add(P_READ);
//        permissions.add(P_UPDATE);
//        permissions.add(P_DELETE);
//
//        permissions.add(P_ADD_USER);
//        permissions.add(P_DELETE_USER);
//        permissions.add(P_UPDATE_ROLE);

        return getAllPermissions();
    }


}