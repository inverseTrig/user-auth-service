package com.example.userauthservice

import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

infix fun LocalDateTime?.shouldSameTime(expected: LocalDateTime?) {
    this?.truncatedTo(ChronoUnit.MILLIS) shouldBe expected?.truncatedTo(ChronoUnit.MILLIS)
}
