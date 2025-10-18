package com.example.userauthservice.domain.user

import com.example.userauthservice.RepositoryTestBase
import com.example.userauthservice.generateString
import com.example.userauthservice.persistAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
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

        context("findByEmail") {
            test("주어진 이메일로 사용자가 존재하면 사용자를 반환한다") {
                // Given
                val email = "test@example.com"

                val user =
                    User(
                        name = generateString(),
                        email = email,
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                entityManager.persist(user)

                // When
                val actual = userRepository.findByEmail(email)

                // Then
                actual shouldBe user
            }

            test("주어진 이메일로 사용자가 존재하지 않으면 null을 반환한다") {
                // Given
                val email = "nonexistent@example.com"

                // When
                val actual = userRepository.findByEmail(email)

                // Then
                actual shouldBe null
            }
        }

        context("UserFilter") {
            test("이름 필터로 사용자를 검색할 수 있다") {
                // Given
                val alice =
                    User(
                        name = "Alice Kim",
                        email = "alice@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )
                val bob =
                    User(
                        name = "Bob Lee",
                        email = "bob@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )
                val alicia =
                    User(
                        name = "Alicia Park",
                        email = "alicia@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                entityManager.persistAll(alice, bob, alicia)

                val filter = UserFilter(name = "ali")
                val pageable = org.springframework.data.domain.PageRequest.of(0, 10)

                // When
                val actual = userRepository.findAll(filter, pageable)

                // Then
                actual.totalElements shouldBe 2
                actual.content.map { it.id } shouldBe listOf(alice.id, alicia.id)
            }

            test("이메일 필터로 사용자를 검색할 수 있다") {
                // Given
                val user1 =
                    User(
                        name = "User One",
                        email = "user1@gmail.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )
                val user2 =
                    User(
                        name = "User Two",
                        email = "user2@yahoo.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )
                val user3 =
                    User(
                        name = "User Three",
                        email = "user3@gmail.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                entityManager.persistAll(user1, user2, user3)

                val filter = UserFilter(email = "gmail")
                val pageable = org.springframework.data.domain.PageRequest.of(0, 10)

                // When
                val actual = userRepository.findAll(filter, pageable)

                // Then
                actual.totalElements shouldBe 2
                actual.content.map { it.id } shouldBe listOf(user1.id, user3.id)
            }
        }
    }
}
