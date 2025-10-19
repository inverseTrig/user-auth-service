package com.example.userauthservice.application.messaging.consumer

import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class UserDeletionEmailConsumer {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitMQConfig.USER_DELETION_EMAIL_QUEUE])
    fun consume(event: UserDeletedEvent) {
        logger.info { "Consuming user deletion email task for userId=${event.userId}" }

        try {
            sendDeletionConfirmationEmail(event)
            logger.info { "Successfully sent deletion email to ${event.email}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send deletion email to ${event.email}" }
            throw e
        }
    }

    private fun sendDeletionConfirmationEmail(event: UserDeletedEvent) {
        logger.info {
            """
            [비동기 작업] 탈퇴 확인 이메일 전송
            - 사용자 ID: ${event.userId}
            - 이메일: ${event.email}
            - 이름: ${event.name}
            - 탈퇴 시간: ${event.deletedAt.format(DateTimeFormatter.ISO_DATE_TIME)}
            """.trimIndent()
        }
    }
}
