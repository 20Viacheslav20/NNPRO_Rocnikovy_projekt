package com.tsystem.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "app",
        indexes = {
                @Index(name = "ux_users_username", columnList = "username", unique = true),
                @Index(name = "ux_users_email",    columnList = "email",    unique = true),
                @Index(name = "idx_users_reset_token", columnList = "reset_token_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 60, unique = true)
    private String username;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String surname;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String password;

    // for password reset
    @Column(name = "reset_code", length = 64)
    private String resetCode;

    @Column(name = "reset_code_exp")
    private OffsetDateTime resetCodeExp;

    @Column(name = "reset_token_id")
    private UUID resetTokenId;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt; // когда пароль менялся в последний раз


    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    private SystemRole role;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}