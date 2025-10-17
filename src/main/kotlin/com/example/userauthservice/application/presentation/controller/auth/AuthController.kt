package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.application.presentation.dto.auth.CreateUserRequest
import com.example.userauthservice.application.presentation.dto.auth.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userFacade: UserFacade,
) {
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val user = userFacade.createUser(request.toData())
        return ResponseEntity.ok(UserResponse(user))
    }
}
