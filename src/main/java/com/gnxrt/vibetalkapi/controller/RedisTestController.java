package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisService redisService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            String testKey = "health_check";
            String testValue = "Redis is working - " + System.currentTimeMillis();

            redisService.setValue(testKey, testValue, 10, TimeUnit.SECONDS);
            Object retrievedValue = redisService.getValue(testKey);

            response.put("status", "UP");
            response.put("message", "Redis connection successful");
            response.put("testValue", retrievedValue);
            response.put("keyExists", redisService.hasKey(testKey));
            response.put("ttl", redisService.getExpire(testKey) + " seconds");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/set")
    public ResponseEntity<String> setValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "300") long ttlSeconds) {

        redisService.setValue(key, value, ttlSeconds, TimeUnit.SECONDS);
        return ResponseEntity.ok("Set key: " + key + " = " + value + " (TTL: " + ttlSeconds + "s)");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getValue(@RequestParam String key) {
        Object value = redisService.getValue(key);
        if (value != null) {
            return ResponseEntity.ok(Map.of(
                    "key", key,
                    "value", value,
                    "ttl", redisService.getExpire(key) + " seconds"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteValue(@RequestParam String key) {
        redisService.deleteValue(key);
        return ResponseEntity.ok("Deleted key: " + key);
    }
}