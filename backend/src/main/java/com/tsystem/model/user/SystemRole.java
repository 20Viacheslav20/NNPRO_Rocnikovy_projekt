package com.tsystem.model.user;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum SystemRole {

    // System-level administrator (full access)
    ADMIN(SystemPermission.all()),

    // Project manager (has full access inside projects he manages)
    PROJECT_MANAGER(Set.of(
            SystemPermission.TICKET_CREATE,
            SystemPermission.TICKET_READ_ALL,
            SystemPermission.TICKET_UPDATE,
            SystemPermission.TICKET_DELETE,
            SystemPermission.PROJECT_READ_ALL,
            SystemPermission.PROJECT_CREATE,
            SystemPermission.PROJECT_UPDATE,
            SystemPermission.PROJECT_DELETE
    )),

    // Regular user (worker)
    USER(Set.of(
            SystemPermission.TICKET_READ_ASSIGNED,
            SystemPermission.TICKET_UPDATE_ASSIGNED,
            SystemPermission.USER_UPDATE_SELF
    ));

    @Getter
    private final Set<SystemPermission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        // Map permissions to Spring Security authorities
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toList());

        // Add role itself as authority (ROLE_ prefix required for Spring)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}
