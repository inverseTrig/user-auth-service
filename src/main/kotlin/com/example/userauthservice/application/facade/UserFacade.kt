package com.example.userauthservice.application.facade

import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.UpdateUserServiceData
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserFilter
import com.example.userauthservice.domain.user.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class UserFacade(
    private val userService: UserService,
) {
    fun createUser(data: CreateUserData): User {
        return userService.create(data)
    }

    fun getUserById(id: Long): User {
        return userService.getById(id)
    }

    fun updateUser(data: UpdateUserServiceData): User {
        return userService.update(data)
    }

    fun getUsersByPage(
        filter: UserFilter,
        pageable: Pageable,
    ): Page<User> {
        return userService.getUsersByPage(filter, pageable)
    }
}
