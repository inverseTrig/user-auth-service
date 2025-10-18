package com.example.userauthservice

import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class FunctionsTest : UnitTestBase() {
    init {
        context("isValidEmail") {
            withData(
                nameFn = { email -> "주어진 이메일이 ($email) 유효하면 true를 반환한다." },
                "user@example.com",
                "test.user@example.com",
                "user+tag@example.co.uk",
                "first.last@subdomain.example.com",
                "user123@test-domain.com",
                "a@b.co",
                "test_user@example.org",
                "user%test@example.net",
                "USER@example.com",
                "user@EXAMPLE.COM",
                "123@456.com",
            ) { email ->
                email.isValidEmail() shouldBe true
            }

            withData(
                nameFn = { email -> "주어진 이메일이 ($email) 유효하지 않으면 false를 반환한다." },
                "",
                "notanemail",
                "@example.com",
                "user@",
                "user@domain",
                "user @example.com",
                "user@exam ple.com",
                "user@@example.com",
                "user@example..com",
                ".user@example.com",
                "user.@example.com",
                "user@.example.com",
                "user@example.com.",
                "user@example",
                "user name@example.com",
                "user@example.c",
                "@",
                "user@domain@example.com",
                " ",
            ) { email ->
                email.isValidEmail() shouldBe false
            }
        }
    }
}
