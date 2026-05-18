package com.example.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        CaffeineCache referenceDataCache = new CaffeineCache(
                "referenceData",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(5))   // TTL for referenceData
                        .maximumSize(500)
                        .build()
        );

        CaffeineCache countryCodesCache = new CaffeineCache(
                "countryCodes",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(1))   // TTL for countryCodes
                        .maximumSize(200)
                        .build()
        );

        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaches(
                java.util.List.of(
                        referenceDataCache,
                        countryCodesCache
                )
        );

        return manager;
    }
}
