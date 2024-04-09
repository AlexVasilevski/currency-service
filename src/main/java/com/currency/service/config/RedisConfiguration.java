package com.currency.service.config;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.*;

import java.io.Serializable;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
public class RedisConfiguration {

    private final int cacheTtlMinutes;
    private final String currencyExchangeRateCacheName;
    private final String availableCurrenciesCacheName;

    public RedisConfiguration(@Value("${service.cache-ttl-minutes}") int cacheTtlMinutes,
                              @Value("${service.currency-exchange-rate-cache}") String currencyExchangeRateCacheName,
                              @Value("${service.available-currencies-cache}") String availableCurrenciesCacheName) {

        this.cacheTtlMinutes = cacheTtlMinutes;
        this.currencyExchangeRateCacheName = currencyExchangeRateCacheName;
        this.availableCurrenciesCacheName = availableCurrenciesCacheName;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisSerializationContext.SerializationPair<Object> serializer = fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        RedisCacheConfiguration currencyRateCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializer)
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes));

        RedisCacheConfiguration availableCurrencyCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializer);

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration(currencyExchangeRateCacheName, currencyRateCacheConfig)
                .withCacheConfiguration(availableCurrenciesCacheName, availableCurrencyCacheConfig)
                .build();
    }

}
