package com.example.userauthservice.domain.user

import com.example.userauthservice.RepositoryTestBase
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.springframework.beans.factory.annotation.Autowired

class UserRepositoryTest : RepositoryTestBase() {
    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        context("existsByEmail") {
            test("주어진 이메일로 사용자가 존재하면 true를 반환한다") {
                // Given
                val email = "test@example.com"

                val user =
                    User(
                        name = "Test User",
                        email = email,
                        password = "encodedPassword123!",
                        role = Role.MEMBER,
                    )

                entityManager.persist(user)

                // When
                val actual = userRepository.existsByEmail(email)

                // Then
                actual.shouldBeTrue()
            }

            test("주어진 이메일로 사용자가 존재하지 않으면 false를 반환한다") {
                // Given
                val email = "nonexistent@example.com"

                // When
                val actual = userRepository.existsByEmail(email)

                // Then
                actual.shouldBeFalse()
            }
        }
    }
}
