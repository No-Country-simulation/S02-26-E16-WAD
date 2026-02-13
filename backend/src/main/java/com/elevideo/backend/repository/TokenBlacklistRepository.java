package com.elevideo.backend.repository;

import com.elevideo.backend.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByToken(String token);

    @Modifying
    void deleteByExpirationDateBefore(LocalDateTime dateTime);

    boolean existsByToken(String token);
}