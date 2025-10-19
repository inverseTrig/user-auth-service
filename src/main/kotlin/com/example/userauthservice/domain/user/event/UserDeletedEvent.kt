package com.example.userauthservice.domain.user.event

import java.time.LocalDateTime

data class UserDeletedEvent(
    val userId: Long,
    val email: String,
    val name: String,
    val deletedAt: LocalDateTime = LocalDateTime.now(),
)
