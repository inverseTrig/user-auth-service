package com.example.userauthservice.application.messaging.consumer

import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class UserDeletionFileConsumer {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitMQConfig.USER_DELETION_FILES_QUEUE])
    fun consume(event: UserDeletedEvent) {
        logger.info { "Consuming user file deletion task for userId=${event.userId}" }

        try {
            deleteUserUploadedFiles(event)
            logger.info { "Successfully deleted files for userId=${event.userId}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete files for userId=${event.userId}" }
            throw e
        }
    }

    private fun deleteUserUploadedFiles(event: UserDeletedEvent) {
        logger.info {
            """
            [비동기 작업] 사용자 파일 삭제
            - 사용자 ID: ${event.userId}
            - 이메일: ${event.email}
            - 이름: ${event.name}
            - 탈퇴 시간: ${event.deletedAt.format(DateTimeFormatter.ISO_DATE_TIME)}
            """.trimIndent()
        }
    }
}
