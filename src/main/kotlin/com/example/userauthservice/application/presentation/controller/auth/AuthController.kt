package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.application.facade.AuthFacade
import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.application.presentation.controller.docs.PostSignIn
import com.example.userauthservice.application.presentation.controller.docs.PostSignUp
import com.example.userauthservice.application.presentation.dto.SignInRequest
import com.example.userauthservice.application.presentation.dto.SignInResponse
import com.example.userauthservice.application.presentation.dto.SignUpRequest
import com.example.userauthservice.application.presentation.dto.SignUpResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Authentication", description = "인증 관리 API")
class AuthController(
    private val userFacade: UserFacade,
    private val authFacade: AuthFacade,
) {
    @PostSignIn
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<SignUpResponse> {
        val user = userFacade.createUser(request.toData())
        return ResponseEntity.ok(SignUpResponse(user))
    }

    @PostSignUp
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
