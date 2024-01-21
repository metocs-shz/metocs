package com.metocs.common.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.metocs.common.cache.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author metocs
 * @date 2024/1/21 15:39
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    @Autowired
    private RedisService redisService;

    @Bean(name = "caffeine")
    public CacheManager caffeineManager(){
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(10) //初始大小
                .maximumSize(100)  //最大大小
                .expireAfterWrite(5, TimeUnit.MINUTES); // 5分钟后过期

        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setAllowNullValues(true);
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }


    @Bean(name = "caffeineCache")
    public Cache<String, Object> cache(){
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(10) //初始大小
                .maximumSize(100)  //最大大小
                .expireAfterWrite(5, TimeUnit.MINUTES); // 5分钟后过期
        Cache<String, Object> caffeineCache = caffeine.build();

        //订阅缓存清理消息
        redisService.subscribe("caffeineCache",new CaffeineSubscribe(caffeineCache));
        return caffeineCache;
    }
}
