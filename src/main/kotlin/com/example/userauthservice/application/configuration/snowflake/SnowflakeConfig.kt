package com.example.userauthservice.application.configuration.snowflake

import com.github.yitter.contract.IdGeneratorOptions
import com.github.yitter.idgen.YitIdHelper
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class SnowflakeConfig(
    private val snowflakeProperties: SnowflakeProperties,
) {
    @PostConstruct
    fun initSnowflake() {
        val options = IdGeneratorOptions(snowflakeProperties.workerId)
        YitIdHelper.setIdGenerator(options)
    }
}
