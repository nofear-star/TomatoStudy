package com.tomato.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单内存黑名单实现，生产请使用 Redis 之类的外部存储
 */
@Service
public class TokenBlacklistService {
    // key: token, value: expiry epoch ms
    private final Map<String, Long> blacklisted = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiryEpochMs) {
        blacklisted.put(token, expiryEpochMs);
    }

    public boolean isBlacklisted(String token) {
        Long exp = blacklisted.get(token);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) {
            blacklisted.remove(token);
            return false;
        }
        return true;
    }
}

