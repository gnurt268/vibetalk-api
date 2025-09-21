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
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set Redis key: {}", key);
        } catch (Exception e) {
            log.error("Error setting Redis key: {}", key, e);
        }
    }

    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set Redis key: {} with TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting Redis key with TTL: {}", key, e);
        }
    }

    public Object getValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Get Redis key: {} = {}", key, value != null ? "found" : "not found");
            return value;
        } catch (Exception e) {
            log.error("Error getting Redis key: {}", key, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting Redis key with type: {}", key, e);
            return null;
        }
    }

    public void deleteValue(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Delete Redis key: {} = {}", key, deleted);
        } catch (Exception e) {
            log.error("Error deleting Redis key: {}", key, e);
        }
    }

    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error deleting keys with pattern: {}", pattern, e);
        }
    }

    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking Redis key existence: {}", key, e);
            return false;
        }
    }

    public void setExpire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
            log.debug("Set expire for key: {} = {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting expire for Redis key: {}", key, e);
        }
    }

    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for Redis key: {}", key, e);
            return -1;
        }
    }
}