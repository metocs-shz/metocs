package com.metocs.common.oauth.resource;

import com.metocs.common.oauth.handler.MyAccessDeniedHandler;
import com.metocs.common.oauth.handler.MyAuthenticationEntryPoint;
import com.metocs.common.oauth.introspect.RedisOpaqueTokenIntrospector;
import com.metocs.common.oauth.open.OpenApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * @author metocs
 * @date 2024/1/21 10:31
 */

@Component
@EnableWebSecurity
public class ResourceServerConfiguration {


    @Autowired
    private OpenApiProperties openApiProperties;


    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain httpSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers(openApiProperties.apis().toArray(new String[]{})).permitAll()
                .anyRequest().authenticated()
        );
        httpSecurity.sessionManagement(AbstractHttpConfigurer::disable);
        httpSecurity.oauth2ResourceServer(resourceServer -> resourceServer
                .accessDeniedHandler(new MyAccessDeniedHandler())
                .authenticationEntryPoint(new MyAuthenticationEntryPoint())
                .opaqueToken(opaque -> opaque.introspector(new RedisOpaqueTokenIntrospector()))
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
