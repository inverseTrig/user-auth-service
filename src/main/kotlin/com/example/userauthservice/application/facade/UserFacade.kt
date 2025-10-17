package com.example.userauthservice.application.facade

import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserService
import org.springframework.stereotype.Component

@Component
class UserFacade(
    private val userService: UserService,
) {
    fun createUser(data: CreateUserData): User {
        return userService.create(data)
    }
}
