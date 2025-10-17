package com.example.userauthservice

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfiguration {
    @Bean
    fun testHelper(context: ApplicationContext) = TestHelper(context)
}
