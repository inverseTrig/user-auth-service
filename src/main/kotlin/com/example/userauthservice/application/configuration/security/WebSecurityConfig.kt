package com.example.userauthservice.application.configuration.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class WebSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/signup").permitAll()
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}
