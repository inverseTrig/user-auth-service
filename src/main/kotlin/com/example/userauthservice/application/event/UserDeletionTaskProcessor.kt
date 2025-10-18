package com.example.userauthservice.application.event

import com.example.userauthservice.application.configuration.rabbitmq.RabbitMQConfig
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class UserDeletionTaskProcessor {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitMQConfig.USER_DELETION_QUEUE])
    fun processUserDeletionTasks(event: UserDeletedEvent) {
        logger.info { "Received UserDeletedEvent from queue: userId=${event.userId}, email=${event.email}" }

        try {
            sendEmailToDeletedUser(event)
            deleteUploadedFiles(event)

            logger.info { "Successfully processed all deletion tasks for userId=${event.userId}" }
        } catch (e: Exception) {
            logger.error(e) { "Error processing user deletion tasks for userId=${event.userId}" }
            throw e // Trigger retry or send to DLQ
        }
    }

    private fun sendEmailToDeletedUser(event: UserDeletedEvent) {
        // TODO: Actual email sending logic would go here
        logger.info {
            """
            [비동기 작업] 탈퇴 사용자에게 이메일 전송
            - 사용자 ID: ${event.userId}
            - 이메일: ${event.email}
            - 이름: ${event.name}
            - 탈퇴 시간: ${event.deletedAt}
            - 내용: 회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.
            """.trimIndent()
        }
    }

    private fun deleteUploadedFiles(event: UserDeletedEvent) {
        // TODO: Actual file deletion logic would go here
        logger.info {
            """
            [비동기 작업] 사용자가 업로드한 파일 삭제
            - 사용자 ID: ${event.userId}
            - 이메일: ${event.email}
            - 작업 내용: 사용자가 업로드한 모든 파일을 삭제합니다.
            - 예상 파일 경로: /uploads/user_${event.userId}/*
            - 파일 삭제 완료 (로그로 대체)
            """.trimIndent()
        }
    }
}
