package com.example.backend.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Deque<Instant>> attemptsByIp = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Deque<Instant> attempts = attemptsByIp.computeIfAbsent(ip, k -> new ArrayDeque<>());
        Instant now = Instant.now();

        synchronized (attempts) {
            clearOldAttempts(attempts, now);

            if (attempts.size() >= MAX_ATTEMPTS) {
                return true;
            }

            attempts.addLast(now);
            return false;
        }
    }

    private void clearOldAttempts(Deque<Instant> attempts, Instant now) {
        while (!attempts.isEmpty()
                && attempts.peekFirst().isBefore(now.minusSeconds(WINDOW_SECONDS))) {
            attempts.pollFirst();
        }
    }
}