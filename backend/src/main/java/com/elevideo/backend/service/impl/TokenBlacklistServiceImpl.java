package com.elevideo.backend.service.impl;

import com.elevideo.backend.model.TokenBlacklist;
import com.elevideo.backend.repository.TokenBlacklistRepository;
import com.elevideo.backend.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Transactional
    public void addTokenToBlacklist(String token, LocalDateTime tokenExpirationDate) {
        TokenBlacklist blacklistedToken = new TokenBlacklist(null, token, tokenExpirationDate);
        tokenBlacklistRepository.save(blacklistedToken);
    }

    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        tokenBlacklistRepository.deleteByExpirationDateBefore(LocalDateTime.now());
    }
}