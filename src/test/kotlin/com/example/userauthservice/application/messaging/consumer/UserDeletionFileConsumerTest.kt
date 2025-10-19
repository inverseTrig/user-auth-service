package com.example.userauthservice.application.messaging.consumer

import ch.qos.logback.classic.Level
import com.example.userauthservice.TestLogAppender
import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.LocalDateTime

class UserDeletionFileConsumerTest : UnitTestBase() {
    private val logAppender = TestLogAppender()

    private val consumer = UserDeletionFileConsumer()

    init {
        context("consume - UserDeletedEvent") {
            test("UserDeletedEvent 를 받아 사용자 파일을 삭제한다.") {
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
                    message = "Consuming user file deletion task for userId=1",
                ).shouldBeTrue()
                logAppender.hasEvent(
                    level = Level.INFO,
                    message =
                        """
                        [비동기 작업] 사용자 파일 삭제
                        - 사용자 ID: 1
                        - 이메일: test@example.com
                        - 이름: Test User
                        - 탈퇴 시간: 2023-12-01T12:00:00
                        """.trimIndent(),
                ).shouldBeTrue()
                logAppender.hasEvent(
                    level = Level.INFO,
                    message = "Successfully deleted files for userId=1",
                ).shouldBeTrue()
            }
        }
    }
}
