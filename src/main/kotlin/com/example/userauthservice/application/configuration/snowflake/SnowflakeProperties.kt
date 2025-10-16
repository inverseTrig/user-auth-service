package com.example.userauthservice.application.configuration.snowflake

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "snowflake")
data class SnowflakeProperties(
    val workerId: Short = 1,
)
