package com.example.userauthservice.application.configuration.rabbitmq

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    companion object {
        // Exchange
        const val USER_EXCHANGE = "user.exchange"

        // Queues
        const val USER_DELETION_QUEUE = "user.deletion.queue"
        const val USER_DELETION_DLQ = "user.deletion.dlq"

        // Routing Keys
        const val USER_DELETED_ROUTING_KEY = "user.deleted"
        const val USER_DELETED_DLQ_ROUTING_KEY = "user.deleted.dlq"
    }

    // ==================== Message Converter ====================

    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    // ==================== Exchange ====================

    @Bean
    fun userExchange(): DirectExchange {
        return DirectExchange(USER_EXCHANGE)
    }

    // ==================== Dead Letter Exchange ====================

    @Bean
    fun deadLetterExchange(): DirectExchange {
        return DirectExchange("$USER_EXCHANGE.dlx")
    }

    // ==================== Main Queue (with DLQ configuration) ====================

    @Bean
    fun userDeletionQueue(): Queue {
        return QueueBuilder.durable(USER_DELETION_QUEUE)
            .withArgument("x-dead-letter-exchange", "$USER_EXCHANGE.dlx")
            .withArgument("x-dead-letter-routing-key", USER_DELETED_DLQ_ROUTING_KEY)
            .build()
    }

    // ==================== Dead Letter Queue ====================

    @Bean
    fun userDeletionDLQ(): Queue {
        return QueueBuilder.durable(USER_DELETION_DLQ).build()
    }

    // ==================== Bindings ====================

    @Bean
    fun userDeletionBinding(
        userDeletionQueue: Queue,
        userExchange: DirectExchange,
    ): Binding {
        return BindingBuilder
            .bind(userDeletionQueue)
            .to(userExchange)
            .with(USER_DELETED_ROUTING_KEY)
    }

    @Bean
    fun userDeletionDLQBinding(
        userDeletionDLQ: Queue,
        deadLetterExchange: DirectExchange,
    ): Binding {
        return BindingBuilder
            .bind(userDeletionDLQ)
            .to(deadLetterExchange)
            .with(USER_DELETED_DLQ_ROUTING_KEY)
    }

    // ==================== RabbitTemplate ====================

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter,
    ): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter

        // Enable publisher confirms for reliable publishing
        rabbitTemplate.setMandatory(true)

        // Set confirm callback (optional - for production monitoring)
        rabbitTemplate.setConfirmCallback { correlationData, ack, cause ->
            if (ack) {
                // Message successfully sent to exchange
                println("Message confirmed: $correlationData")
            } else {
                // Message failed to reach exchange
                println("Message failed: $correlationData, cause: $cause")
            }
        }

        // Set return callback (handles unroutable messages)
        rabbitTemplate.setReturnsCallback { returned ->
            println(
                "Message returned: ${returned.message}, " +
                    "replyCode: ${returned.replyCode}, " +
                    "replyText: ${returned.replyText}",
            )
        }

        return rabbitTemplate
    }
}
