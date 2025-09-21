package com.gnxrt.vibetalkapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Cache keys
    private static final String USER_PROFILE_KEY = "user:profile:";
    private static final String CHAT_MESSAGES_KEY = "chat:messages:";
    private static final String USER_CHATS_KEY = "user:chats:";
    private static final String ONLINE_USERS_KEY = "users:online";
    private static final String USER_TYPING_KEY = "typing:";

    // Cache TTL
    private static final long USER_CACHE_TTL = 30;
    private static final long MESSAGE_CACHE_TTL = 15;
    private static final long CHAT_CACHE_TTL = 20;

    public void cacheUserProfile(Integer userId, Object userProfile) {
        try {
            String key = USER_PROFILE_KEY + userId;
            redisTemplate.opsForValue().set(key, userProfile, USER_CACHE_TTL, TimeUnit.MINUTES);
            log.debug("Cached user profile for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error caching user profile for userId: {}", userId, e);
        }
    }

    public Object getCachedUserProfile(Integer userId) {
        try {
            String key = USER_PROFILE_KEY + userId;
            Object profile = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved cached user profile for userId: {}", userId);
            return profile;
        } catch (Exception e) {
            log.error("Error getting cached user profile for userId: {}", userId, e);
            return null;
        }
    }

    public void cacheChatMessages(Integer chatId, Object messages) {
        try {
            String key = CHAT_MESSAGES_KEY + chatId;
            redisTemplate.opsForValue().set(key, messages, MESSAGE_CACHE_TTL, TimeUnit.MINUTES);
            log.debug("Cached messages for chatId: {}", chatId);
        } catch (Exception e) {
            log.error("Error caching messages for chatId: {}", chatId, e);
        }
    }

    public Object getCachedChatMessages(Integer chatId) {
        try {
            String key = CHAT_MESSAGES_KEY + chatId;
            Object messages = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved cached messages for chatId: {}", chatId);
            return messages;
        } catch (Exception e) {
            log.error("Error getting cached messages for chatId: {}", chatId, e);
            return null;
        }
    }

    public void cacheUserChats(Integer userId, Object chats) {
        try {
            String key = USER_CHATS_KEY + userId;
            redisTemplate.opsForValue().set(key, chats, CHAT_CACHE_TTL, TimeUnit.MINUTES);
            log.debug("Cached chats for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error caching chats for userId: {}", userId, e);
        }
    }

    public Object getCachedUserChats(Integer userId) {
        try {
            String key = USER_CHATS_KEY + userId;
            Object chats = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved cached chats for userId: {}", userId);
            return chats;
        } catch (Exception e) {
            log.error("Error getting cached chats for userId: {}", userId, e);
            return null;
        }
    }

    public void addUserOnline(Integer userId) {
        try {
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
            redisTemplate.expire(ONLINE_USERS_KEY, 30, TimeUnit.MINUTES);
            log.debug("Added user {} to online users", userId);
        } catch (Exception e) {
            log.error("Error adding user {} to online users", userId, e);
        }
    }

    public void removeUserOnline(Integer userId) {
        try {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
            log.debug("Removed user {} from online users", userId);
        } catch (Exception e) {
            log.error("Error removing user {} from online users", userId, e);
        }
    }

    public boolean isUserOnline(Integer userId) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString());
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Error checking if user {} is online", userId, e);
            return false;
        }
    }

    public Set<Object> getOnlineUsers() {
        try {
            return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        } catch (Exception e) {
            log.error("Error getting online users", e);
            return Set.of();
        }
    }

    public void setUserTyping(Integer chatId, Integer userId, boolean isTyping) {
        try {
            String key = USER_TYPING_KEY + chatId + ":" + userId;
            if (isTyping) {
                redisTemplate.opsForValue().set(key, "typing", 10, TimeUnit.SECONDS);
                log.debug("Set typing indicator for user {} in chat {}", userId, chatId);
            } else {
                redisTemplate.delete(key);
                log.debug("Removed typing indicator for user {} in chat {}", userId, chatId);
            }
        } catch (Exception e) {
            log.error("Error setting typing indicator for user {} in chat {}", userId, chatId, e);
        }
    }

    public void clearChatMessagesCache(Integer chatId) {
        try {
            redisTemplate.delete(CHAT_MESSAGES_KEY + chatId);
            log.debug("Cleared messages cache for chatId: {}", chatId);
        } catch (Exception e) {
            log.error("Error clearing messages cache for chatId: {}", chatId, e);
        }
    }

    public void clearUserChatsCache(Integer userId) {
        try {
            redisTemplate.delete(USER_CHATS_KEY + userId);
            log.debug("Cleared chats cache for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error clearing chats cache for userId: {}", userId, e);
        }
    }

    public void clearUserCache(Integer userId) {
        try {
            redisTemplate.delete(USER_PROFILE_KEY + userId);
            redisTemplate.delete(USER_CHATS_KEY + userId);
            removeUserOnline(userId);
            log.debug("Cleared all cache for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error clearing cache for userId: {}", userId, e);
        }
    }

    public boolean isRedisHealthy() {
        try {
            String testKey = "health_check_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "ok", 5, TimeUnit.SECONDS);
            Object value = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            return "ok".equals(value);
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }
}