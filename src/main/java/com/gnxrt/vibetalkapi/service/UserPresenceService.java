package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserPresenceService {

    private final ConcurrentMap<Integer, Set<String>> userSessions = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, LocalDateTime> lastSeenMap = new ConcurrentHashMap<>();

    public void markUserOnline(User user, String sessionId) {
        userSessions.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        lastSeenMap.put(user.getId(), LocalDateTime.now());
    }

    public void markUserOffline(User user, String sessionId) {
        Set<String> sessions = userSessions.get(user.getId());
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(user.getId());
            }
        }
        lastSeenMap.put(user.getId(), LocalDateTime.now());
    }

    public boolean isUserOnline(Integer userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public LocalDateTime getLastSeen(Integer userId) {
        return lastSeenMap.get(userId);
    }

    public Set<Integer> getOnlineUserIds() {
        return userSessions.keySet();
    }

    public int getActiveSessionCount(Integer userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    public void removeAllUserSessions(Integer userId) {
        userSessions.remove(userId);
        lastSeenMap.put(userId, LocalDateTime.now());
    }
}