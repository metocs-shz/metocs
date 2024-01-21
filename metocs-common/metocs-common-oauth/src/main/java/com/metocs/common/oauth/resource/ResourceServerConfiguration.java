package com.metocs.common.oauth.resource;

import com.metocs.common.oauth.handler.MyAccessDeniedHandler;
import com.metocs.common.oauth.handler.MyAuthenticationEntryPoint;
import com.metocs.common.oauth.open.OpenApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;

/**
 * @author metocs
 * @date 2024/1/21 10:31
 */
public class ResourceServerConfiguration {


    @Autowired
    private OpenApiProperties openApiProperties;


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
                .jwt(Customizer.withDefaults())
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("x.509");
        ClassPathResource resource = new ClassPathResource("public.jks");
        Certificate certificate = certificateFactory.generateCertificate(resource.getInputStream());
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
