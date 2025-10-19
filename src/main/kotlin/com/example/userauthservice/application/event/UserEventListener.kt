package com.example.userauthservice.application.event

import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val rabbitTemplate: RabbitTemplate,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: UserDeletedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.USER_EVENTS_EXCHANGE,
            "",
            event,
        )
    }
}
