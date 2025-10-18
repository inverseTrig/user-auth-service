package com.example.userauthservice.application.event

import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger = KotlinLogging.logger {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserDeleted(event: UserDeletedEvent) {
        try {
            logger.info { "Publishing user deletion event to RabbitMQ: userId=${event.userId}" }

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_DELETED_ROUTING_KEY,
                event,
            )

            logger.info { "Successfully published user deletion event: userId=${event.userId}" }
        } catch (e: Exception) {
            // Log error but don't fail the transaction (user already deleted)
            logger.error(e) { "Failed to publish user deletion event to RabbitMQ: userId=${event.userId}" }
            // Could implement retry logic or alert monitoring system
        }
    }
}
