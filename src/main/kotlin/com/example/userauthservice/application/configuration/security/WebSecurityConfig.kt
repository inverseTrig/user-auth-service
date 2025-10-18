package com.example.userauthservice.application.configuration.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableConfigurationProperties(CorsProperties::class)
class WebSecurityConfig(
    private val corsProperties: CorsProperties,
) {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/signup").permitAll()
                    .requestMatchers("/signin").permitAll()
                    .anyRequest().authenticated()
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        if (corsProperties.allowedOrigins.isNotEmpty()) {
            configuration.allowedOrigins = corsProperties.allowedOrigins
        }

        if (corsProperties.allowedOriginPatterns.isNotEmpty()) {
            configuration.allowedOriginPatterns = corsProperties.allowedOriginPatterns
        }

        configuration.allowedMethods = corsProperties.allowedMethods
        configuration.allowedHeaders = corsProperties.allowedHeaders
        configuration.allowCredentials = corsProperties.allowCredentials
        configuration.maxAge = corsProperties.maxAge

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
