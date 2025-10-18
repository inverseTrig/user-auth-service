package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.application.facade.AuthFacade
import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.application.presentation.dto.auth.SignInRequest
import com.example.userauthservice.application.presentation.dto.auth.SignInResponse
import com.example.userauthservice.application.presentation.dto.auth.SignUpRequest
import com.example.userauthservice.application.presentation.dto.auth.SignUpResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userFacade: UserFacade,
    private val authFacade: AuthFacade,
) {
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<SignUpResponse> {
        val user = userFacade.createUser(request.toData())
        return ResponseEntity.ok(SignUpResponse(user))
    }

    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> {
        val info =
            authFacade.authenticate(
                email = request.email,
                password = request.password,
            )
        return ResponseEntity.ok(SignInResponse(info))
    }
}
