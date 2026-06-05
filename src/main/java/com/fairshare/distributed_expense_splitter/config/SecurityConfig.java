package com.fairshare.distributed_expense_splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF to unlock POST/PUT/DELETE requests globally
            .csrf(csrf -> csrf.disable());
            
            // 2. Configure endpoint authorization
            // .authorizeHttpRequests(auth -> auth
            //     .requestMatchers("/api/**").permitAll() // Public POST endpoints
            //     .anyRequest().authenticated()                 // Protected endpoints
            // );

        return http.build();
    }
}