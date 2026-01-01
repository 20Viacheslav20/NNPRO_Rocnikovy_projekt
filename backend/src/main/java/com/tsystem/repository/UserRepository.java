package com.tsystem.repository;
import com.tsystem.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.blocked = true, u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :userId")
    int blockUser(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.blocked = false, u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :userId")
    int unblockUser(@Param("userId") UUID userId);
}