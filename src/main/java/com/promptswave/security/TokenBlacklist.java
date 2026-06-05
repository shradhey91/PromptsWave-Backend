package com.promptswave.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        Instant expiry = (expiresAt != null) ? expiresAt : Instant.now().plusSeconds(3600);
        blacklistedTokens.put(token, expiry);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedRateString = "${token.blacklist.cleanup-interval-ms:3600000}")
    public void evictExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry ->
                entry.getValue() != null && entry.getValue().isBefore(now));
    }
}
