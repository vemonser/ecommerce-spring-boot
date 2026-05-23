package com.codencanvas.ecommerce.infrastructure.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp, LimitType type) {
        String key = type.name().toLowerCase() + ":" + clientIp;

        Bucket bucket = ipBuckets.computeIfAbsent(key, k -> createBucket(type));

        return bucket.tryConsume(1);
    }

    private Bucket createBucket(LimitType type) {
        return switch (type) {
            case LOGIN -> Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(5)
                            .refillIntervally(5, java.time.Duration.ofMinutes(1))
                            .build())
                    .build();
            case REGISTER -> Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(3)
                            .refillIntervally(3, java.time.Duration.ofHours(1))
                            .build())
                    .build();
            case FORGOT_PASSWORD -> Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(3)
                            .refillIntervally(3, java.time.Duration.ofHours(1))
                            .build())
                    .build();
            case GENERAL -> Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(100)
                            .refillIntervally(100, java.time.Duration.ofMinutes(1))
                            .build())
                    .build();
        };
    }

}
