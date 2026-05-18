@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public javax.cache.CacheManager jCacheManager() {

        // Build the underlying Ehcache 3 manager
        org.ehcache.CacheManager ehcacheManager =
                CacheManagerBuilder.newCacheManagerBuilder().build(true);

        // ---- Cache 1: 1-minute TTL ----
        CacheConfiguration<String, Object> oneMinuteEhcacheConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                String.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(500)
                        )
                        .withExpiry(
                                ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(1))
                        )
                        .build();

        CompleteConfiguration<String, Object> oneMinuteJCacheConfig =
                Eh107Configuration.fromEhcacheCacheConfiguration(oneMinuteEhcacheConfig);

        // ---- Cache 2: 10-minute TTL ----
        CacheConfiguration<String, Object> tenMinuteEhcacheConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                String.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(500)
                        )
                        .withExpiry(
                                ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))
                        )
                        .build();

        CompleteConfiguration<String, Object> tenMinuteJCacheConfig =
                Eh107Configuration.fromEhcacheCacheConfiguration(tenMinuteEhcacheConfig);

        // ---- Create JCache manager ----
        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager jcacheManager = provider.getCacheManager();

        // Register caches
        jcacheManager.createCache("oneMinuteCache", oneMinuteJCacheConfig);
        jcacheManager.createCache("tenMinuteCache", tenMinuteJCacheConfig);

        return jcacheManager;
    }

    @Bean
    public org.springframework.cache.CacheManager springCacheManager(javax.cache.CacheManager jCacheManager) {
        return new JCacheCacheManager(jCacheManager);
    }
}
