package com.example.userauthservice.application.configuration.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOrigins: List<String> = emptyList(),
    val allowedOriginPatterns: List<String> = emptyList(),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
    val allowedHeaders: List<String> = listOf("*"),
    val allowCredentials: Boolean = true,
    val maxAge: Long = 3600,
)
