package com.tsystem.model.user;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tsystem.model.user.SystemPermission.getAdminPermissions;
import static com.tsystem.model.user.SystemPermission.getUserPermissions;

@RequiredArgsConstructor
public enum SystemRole {

    //system level roles
    SYSTEM_USER(getUserPermissions()),
    SYSTEM_ADMIN(getAdminPermissions()),
    SYSTEM_RESEARCHER(getUserPermissions());

    @Getter
    private final Set<SystemPermission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority(this.name()));
        return authorities;
    }
}