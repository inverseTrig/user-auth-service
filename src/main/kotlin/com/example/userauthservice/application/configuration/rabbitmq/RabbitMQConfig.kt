package com.example.userauthservice.application.configuration.rabbitmq

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.FanoutExchange
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
    private val logger = KotlinLogging.logger {}

    companion object {
        // Exchanges
        const val USER_EVENTS_EXCHANGE = "user.events.fanout"
        const val USER_DLX_EXCHANGE = "user.events.dlx"

        // Queues
        const val USER_DELETION_EMAIL_QUEUE = "user.deletion.email.queue"
        const val USER_DELETION_FILES_QUEUE = "user.deletion.files.queue"

        // Dead Letter Queues
        const val USER_DELETION_EMAIL_DLQ = "user.deletion.email.dlq"
        const val USER_DELETION_FILES_DLQ = "user.deletion.files.dlq"

        // Routing Keys
        const val EMAIL_DLQ_ROUTING_KEY = "user.deletion.email.failed"
        const val FILES_DLQ_ROUTING_KEY = "user.deletion.files.failed"
    }

    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    // Exchanges

    @Bean
    fun userEventsExchange(): FanoutExchange {
        return FanoutExchange(USER_EVENTS_EXCHANGE, true, false)
    }

    @Bean
    fun deadLetterExchange(): DirectExchange {
        return DirectExchange(USER_DLX_EXCHANGE, true, false)
    }

    // Queues

    @Bean
    fun userDeletionEmailQueue(): Queue {
        return QueueBuilder.durable(USER_DELETION_EMAIL_QUEUE)
            .withArgument("x-dead-letter-exchange", USER_DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", EMAIL_DLQ_ROUTING_KEY)
            .build()
    }

    @Bean
    fun userDeletionFilesQueue(): Queue {
        return QueueBuilder.durable(USER_DELETION_FILES_QUEUE)
            .withArgument("x-dead-letter-exchange", USER_DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", FILES_DLQ_ROUTING_KEY)
            .build()
    }

    // Dead Letter Queues

    @Bean
    fun userDeletionEmailDLQ(): Queue {
        return QueueBuilder.durable(USER_DELETION_EMAIL_DLQ).build()
    }

    @Bean
    fun userDeletionFilesDLQ(): Queue {
        return QueueBuilder.durable(USER_DELETION_FILES_DLQ).build()
    }

    // Bindings

    @Bean
    fun emailQueueBinding(
        userDeletionEmailQueue: Queue,
        userEventsExchange: FanoutExchange,
    ): Binding {
        return BindingBuilder.bind(userDeletionEmailQueue).to(userEventsExchange)
    }

    @Bean
    fun filesQueueBinding(
        userDeletionFilesQueue: Queue,
        userEventsExchange: FanoutExchange,
    ): Binding {
        return BindingBuilder.bind(userDeletionFilesQueue).to(userEventsExchange)
    }

    @Bean
    fun emailDLQBinding(
        userDeletionEmailDLQ: Queue,
        deadLetterExchange: DirectExchange,
    ): Binding {
        return BindingBuilder
            .bind(userDeletionEmailDLQ)
            .to(deadLetterExchange)
            .with(EMAIL_DLQ_ROUTING_KEY)
    }

    @Bean
    fun filesDLQBinding(
        userDeletionFilesDLQ: Queue,
        deadLetterExchange: DirectExchange,
    ): Binding {
        return BindingBuilder
            .bind(userDeletionFilesDLQ)
            .to(deadLetterExchange)
            .with(FILES_DLQ_ROUTING_KEY)
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter,
    ): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter

        rabbitTemplate.setMandatory(true)

        rabbitTemplate.setConfirmCallback { correlationData, ack, cause ->
            if (ack) {
                logger.info { "Message confirmed: $correlationData" }
            } else {
                logger.info { "Message failed: $correlationData, cause: $cause" }
            }
        }

        rabbitTemplate.setReturnsCallback { returned ->
            logger.error {
                "Message returned: ${returned.message}, " +
                    "replyCode: ${returned.replyCode}, " +
                    "replyText: ${returned.replyText}"
            }
        }

        return rabbitTemplate
    }
}
