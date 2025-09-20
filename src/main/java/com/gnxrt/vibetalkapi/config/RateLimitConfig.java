package com.gnxrt.vibetalkapi.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public enum RateLimitType {
        LOGIN(5, Duration.ofMinutes(15)),
        REGISTER(3, Duration.ofMinutes(60)),
        SEND_MESSAGE(100, Duration.ofMinutes(1)),
        UPLOAD_FILE(10, Duration.ofMinutes(5)),
        FORGOT_PASSWORD(3, Duration.ofMinutes(5)),
        RESET_PASSWORD(5, Duration.ofMinutes(15)),
        GENERAL_API(200, Duration.ofMinutes(1));

        private final long capacity;
        private final Duration refillDuration;

        RateLimitType(long capacity, Duration refillDuration) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
        }

        public long getCapacity() {
            return capacity;
        }

        public Duration getRefillDuration() {
            return refillDuration;
        }
    }

    public boolean isAllowed(String identifier, RateLimitType limitType) {
        String key = limitType.name() + ":" + identifier;
        Bucket bucket = getBucket(key, limitType);
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String identifier, RateLimitType limitType) {
        String key = limitType.name() + ":" + identifier;
        Bucket bucket = getBucket(key, limitType);
        return bucket.getAvailableTokens();
    }

    private Bucket getBucket(String key, RateLimitType limitType) {
        return bucketCache.computeIfAbsent(key, k -> createNewBucket(limitType));
    }

    private Bucket createNewBucket(RateLimitType limitType) {
        Bandwidth limit = Bandwidth.classic(
                limitType.getCapacity(),
                Refill.intervally(limitType.getCapacity(), limitType.getRefillDuration())
        );
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}