package com.example.backend.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GlobalRateLimitService {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Deque<Instant>> requestsByIp = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Instant now = Instant.now();
        Deque<Instant> requests = requestsByIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (requests) {
            removeExpiredRequests(requests, now);

            if (requests.size() >= MAX_REQUESTS) {
                return true;
            }

            requests.addLast(now);
            return false;
        }
    }

    private void removeExpiredRequests(Deque<Instant> requests, Instant now) {
        Instant limit = now.minusSeconds(WINDOW_SECONDS);

        while (!requests.isEmpty() && requests.peekFirst().isBefore(limit)) {
            requests.pollFirst();
        }
    }
}