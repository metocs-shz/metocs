package com.metocs.authorization;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author metocs
 * @date 2024/1/21 17:14
 */

@EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.metocs")
@MapperScan("com.metocs.**.mapper")
@EnableRedisHttpSession
public class AuthorizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationApplication.class);
    }
}
