package com.tsystem.repository;

import com.tsystem.model.user.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PasswordResetTokenRepository  extends JpaRepository<PasswordResetToken, UUID> {

    @Modifying
    @Query("delete from PasswordResetToken t where t.user.id = :userId")
    void deleteAllByUserId(UUID userId);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :now")
    void deleteExpired(OffsetDateTime now);
}
