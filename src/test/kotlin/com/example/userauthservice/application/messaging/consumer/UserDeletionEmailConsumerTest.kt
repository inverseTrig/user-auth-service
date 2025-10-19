package com.example.userauthservice.application.messaging.consumer

import ch.qos.logback.classic.Level
import com.example.userauthservice.TestLogAppender
import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.LocalDateTime

class UserDeletionEmailConsumerTest : UnitTestBase() {
    private val logAppender = TestLogAppender()

    private val consumer = UserDeletionEmailConsumer()

    init {
        context("consume - UserDeletedEvent") {
            test("UserDeletedEvent 를 받아 삭제 확인 이메일을 전송한다.") {
                // Given
                val event =
                    UserDeletedEvent(
                        userId = 1L,
                        email = "test@example.com",
                        name = "Test User",
                        deletedAt = LocalDateTime.of(2023, 12, 1, 12, 0, 0),
                    )

                // When
                consumer.consume(event)

                // Then
                logAppender.hasEvent(
                    level = Level.INFO,
                    message = "Consuming user deletion email task for userId=1",
                ).shouldBeTrue()
                logAppender.hasEvent(
                    level = Level.INFO,
                    message =
                        """
                        [비동기 작업] 탈퇴 확인 이메일 전송
                        - 사용자 ID: 1
                        - 이메일: test@example.com
                        - 이름: Test User
                        - 탈퇴 시간: 2023-12-01T12:00:00
                        """.trimIndent(),
                ).shouldBeTrue()
                logAppender.hasEvent(
                    level = Level.INFO,
                    message = "Successfully sent deletion email to test@example.com",
                ).shouldBeTrue()
            }
        }
    }
}
