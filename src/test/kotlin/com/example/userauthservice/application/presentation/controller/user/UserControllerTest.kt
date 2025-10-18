package com.example.userauthservice.application.presentation.controller.user

import com.example.userauthservice.FunctionalTestBase
import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.UpdateUserRequest
import com.example.userauthservice.application.presentation.dto.UserResponse
import com.example.userauthservice.domain.user.Role
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus

class UserControllerTest : FunctionalTestBase() {
    init {
        context("getById") {
            test("ADMIN 권한으로 본인의 정보를 조회할 수 있다.") {
                // Given
                val adminUser =
                    testHelper.createUser(
                        name = "AdminTest",
                        email = "admin-get-by-id@example.com",
                        role = Role.ADMIN,
                    )

                val token = testHelper.generateToken(adminUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${adminUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe adminUser.id
                    it.name shouldBe "AdminTest"
                    it.email shouldBe "admin-get-by-id@example.com"
                    it.role shouldBe "ADMIN"
                }
            }

            test("ADMIN 권한으로 다른 사용자의 정보를 조회할 수 있다.") {
                // Given
                val adminUser = testHelper.createUser(role = Role.ADMIN)

                val memberUser =
                    testHelper.createUser(
                        name = "AnotherMember",
                        email = "member-to-view@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(adminUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${memberUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "AnotherMember"
                    it.email shouldBe "member-to-view@example.com"
                    it.role shouldBe "MEMBER"
                }
            }

            test("MEMBER 권한으로 본인의 정보를 조회할 수 있다.") {
                // Given
                val memberUser =
                    testHelper.createUser(
                        name = "MemberTest",
                        email = "member-self@test.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(memberUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${memberUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "MemberTest"
                    it.email shouldBe "member-self@test.com"
                    it.role shouldBe "MEMBER"
                }
            }

            test("MEMBER 권한으로 다른 사용자의 정보를 조회하면 오류를 발생한다.") {
                // Given
                val memberUser = testHelper.createUser(role = Role.MEMBER)
                val otherMember = testHelper.createUser(role = Role.MEMBER)

                val token = testHelper.generateToken(memberUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${otherMember.id}",
                        ErrorResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.FORBIDDEN

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.FORBIDDEN.value()
                    it.error shouldBe "Forbidden"
                    it.message shouldBe "Access Denied"
                    it.path shouldBe "/api/users/${otherMember.id}"
                }
            }
        }

        context("updateUser") {
            test("ADMIN 권한으로 본인의 정보를 수정할 수 있다.") {
                // Given
                val adminUser =
                    testHelper.createUser(
                        name = "AdminOriginal",
                        email = "admin-update-self@example.com",
                        role = Role.ADMIN,
                    )

                val token = testHelper.generateToken(adminUser)

                val request =
                    UpdateUserRequest(
                        name = "AdminUpdated",
                        email = "admin-updated@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${adminUser.id}",
                        request,
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe adminUser.id
                    it.name shouldBe "AdminUpdated"
                    it.email shouldBe "admin-updated@example.com"
                }

                val updatedUser = testHelper.getUser(adminUser.id)
                assertSoftly(updatedUser) {
                    it.name shouldBe "AdminUpdated"
                    it.email shouldBe "admin-updated@example.com"
                }
            }

            test("ADMIN 권한으로 다른 사용자의 정보를 수정할 수 있다.") {
                // Given
                val adminUser = testHelper.createUser(role = Role.ADMIN)

                val memberUser =
                    testHelper.createUser(
                        name = "MemberOriginal",
                        email = "member-to-update@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(adminUser)

                val request =
                    UpdateUserRequest(
                        name = "MemberUpdated",
                        email = "member-updated@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${memberUser.id}",
                        request,
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.name shouldBe "MemberUpdated"
                    it.email shouldBe "member-updated@example.com"
                }

                val updatedUser = testHelper.getUser(memberUser.id)
                assertSoftly(updatedUser) {
                    it.name shouldBe "MemberUpdated"
                    it.email shouldBe "member-updated@example.com"
                }
            }

            test("MEMBER 권한으로 본인의 정보를 수정할 수 있다.") {
                // Given
                val memberUser =
                    testHelper.createUser(
                        name = "MemberOriginal",
                        email = "member-update-self@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(memberUser)

                val request =
                    UpdateUserRequest(
                        name = "MemberUpdated",
                        email = "member-self-updated@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${memberUser.id}",
                        request,
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "MemberUpdated"
                    it.email shouldBe "member-self-updated@example.com"
                }

                val updatedUser = testHelper.getUser(memberUser.id)
                assertSoftly(updatedUser) {
                    it.name shouldBe "MemberUpdated"
                    it.email shouldBe "member-self-updated@example.com"
                }
            }

            test("MEMBER 권한으로 다른 사용자의 정보를 수정하면 오류를 발생한다.") {
                // Given
                val memberUser = testHelper.createUser(role = Role.MEMBER)
                val otherMember = testHelper.createUser(role = Role.MEMBER)

                val token = testHelper.generateToken(memberUser)

                val request =
                    UpdateUserRequest(
                        name = "HackedName",
                        email = "hacked@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${otherMember.id}",
                        request,
                        ErrorResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.FORBIDDEN

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.FORBIDDEN.value()
                    it.error shouldBe "Forbidden"
                    it.message shouldBe "Access Denied"
                    it.path shouldBe "/api/users/${otherMember.id}"
                }
            }

            test("유효하지 않은 이메일 형식으로 수정하면 오류를 발생한다.") {
                // Given
                val adminUser = testHelper.createUser(role = Role.ADMIN)

                val token = testHelper.generateToken(adminUser)

                val request =
                    UpdateUserRequest(
                        name = "UpdatedName",
                        email = "invalid_email",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${adminUser.id}",
                        request,
                        ErrorResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.BAD_REQUEST

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.BAD_REQUEST.value()
                    it.error shouldBe "Bad Request"
                    it.message shouldBe "Invalid email format"
                    it.path shouldBe "/api/users/${adminUser.id}"
                }
            }

            test("이름만 수정할 수 있다.") {
                // Given
                val memberUser =
                    testHelper.createUser(
                        name = "OriginalName",
                        email = "updating-name@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(memberUser)

                val request =
                    UpdateUserRequest(
                        name = "UpdatedNameOnly",
                        email = null,
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${memberUser.id}",
                        request,
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "UpdatedNameOnly"
                    it.email shouldBe "updating-name@example.com"
                }

                val updatedUser = testHelper.getUser(memberUser.id)
                assertSoftly(updatedUser) {
                    it.name shouldBe "UpdatedNameOnly"
                    it.email shouldBe "updating-name@example.com"
                }
            }

            test("이메일만 수정할 수 있다.") {
                // Given
                val memberUser =
                    testHelper.createUser(
                        name = "OriginalName",
                        email = "updating-email@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(memberUser)

                val request =
                    UpdateUserRequest(
                        name = null,
                        email = "updated-email@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${memberUser.id}",
                        request,
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "OriginalName"
                    it.email shouldBe "updated-email@example.com"
                }

                val updatedUser = testHelper.getUser(memberUser.id)
                assertSoftly(updatedUser) {
                    it.name shouldBe "OriginalName"
                    it.email shouldBe "updated-email@example.com"
                }
            }

            test("이미 사용 중인 이메일로 수정하면 오류를 발생한다.") {
                // Given
                val user1 =
                    testHelper.createUser(
                        name = "User1",
                        email = "user1-duplicate-check@example.com",
                    )

                val user2 =
                    testHelper.createUser(
                        name = "User2",
                        email = "user2-duplicate-check@example.com",
                    )

                val token = testHelper.generateToken(user1)

                val request =
                    UpdateUserRequest(
                        name = "User1Updated",
                        email = "user2-duplicate-check@example.com",
                    )

                // When
                val actual =
                    client.putForEntity(
                        "/users/${user1.id}",
                        request,
                        ErrorResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.BAD_REQUEST

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.BAD_REQUEST.value()
                    it.error shouldBe "Bad Request"
                    it.message shouldBe "Email already exists"
                    it.path shouldBe "/api/users/${user1.id}"
                }
            }
        }
    }
}
