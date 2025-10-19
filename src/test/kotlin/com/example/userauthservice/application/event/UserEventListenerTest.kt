package com.example.userauthservice.application.event

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.mockk.mockk
import io.mockk.verify
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.LocalDateTime

class UserEventListenerTest : UnitTestBase() {
    private val rabbitTemplate = mockk<RabbitTemplate>()
    private val userEventListener = UserEventListener(rabbitTemplate)

    init {
        context("handle - UserDeletedEvent") {
            test("UserDeletedEvent 를 받아 RabbitMQ Exchange 에 발송한다.") {
                // Given
                val event =
                    UserDeletedEvent(
                        userId = 1L,
                        email = "test@example.com",
                        name = "Test User",
                        deletedAt = LocalDateTime.now(),
                    )

                // When
                userEventListener.handle(event)

                // Then
                verify {
                    rabbitTemplate.convertAndSend(
                        RabbitMQConfig.USER_EVENTS_EXCHANGE,
                        "",
                        event,
                    )
                }
            }
        }
    }
}
