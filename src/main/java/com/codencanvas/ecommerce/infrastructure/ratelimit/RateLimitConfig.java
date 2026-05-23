package com.codencanvas.ecommerce.infrastructure.ratelimit;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;

import io.github.bucket4j.Bucket;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Bean
    public Bucket registerBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofHours(1))
                        .build())
                .build();
    }

    @Bean
    public Bucket forgotPasswordBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofHours(1))
                        .build())
                .build();
    }

    @Bean
    public Bucket generalBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillIntervally(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }

}
