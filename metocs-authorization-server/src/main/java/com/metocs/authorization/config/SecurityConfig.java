package com.metocs.authorization.config;

import com.metocs.authorization.filter.CaptchaUsernamePasswordFilter;
import com.metocs.authorization.filter.SmsAuthenticationProcessingFilter;
import com.metocs.authorization.provider.MyDaoAuthenticationProvider;
import com.metocs.authorization.provider.SmsCodeAuthenticationProvider;
import com.metocs.common.cache.RedisService;
import com.metocs.common.oauth.handler.MyAccessDeniedHandler;
import com.metocs.common.oauth.introspect.RedisOpaqueTokenIntrospector;
import com.metocs.common.oauth.mapper.Oauth2ClientMapper;
import com.metocs.common.oauth.service.Oauth2ClientRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author metocs
 * @date 2024/1/21 10:25
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private Oauth2ClientMapper oauth2ClientMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisService redisService;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0
        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedHandler(new MyAccessDeniedHandler())
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) ->
                        resourceServer.opaqueToken(opaqueTokenConfigurer ->
                                opaqueTokenConfigurer.introspector(new RedisOpaqueTokenIntrospector())));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/code"),
                                new AntPathRequestMatcher("/captcha/login"),
                                new AntPathRequestMatcher("/mobile/login")).permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        ProviderManager providerManager = providerManager();
        // 添加用户名密码登录方式
        http.addFilter(new CaptchaUsernamePasswordFilter(providerManager));
        // 添加手机号验证码登录方式
        http.addFilter(new SmsAuthenticationProcessingFilter(providerManager));
        return http.build();
    }

    public ProviderManager providerManager(){
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new MyDaoAuthenticationProvider());
        providers.add(new SmsCodeAuthenticationProvider(userDetailsService,redisService));
        return new ProviderManager(providers);
    }


    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new Oauth2ClientRepositoryService(oauth2ClientMapper);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
