package com.fairshare.distributed_expense_splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web ->
      web
        .ignoring()
        .requestMatchers(
          PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**")
        );
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // Disables CSRF globally to unlock POST/PUT/DELETE for your APIs
      .csrf(csrf -> csrf.disable());

    return http.build();
  }
}
