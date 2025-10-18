package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.application.facade.AuthFacade
import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.SignInRequest
import com.example.userauthservice.application.presentation.dto.SignInResponse
import com.example.userauthservice.application.presentation.dto.SignUpRequest
import com.example.userauthservice.application.presentation.dto.SignUpResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @PostMapping("/signup")
    @Operation(summary = "사용자 가입")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 등록 성공",
                content = [Content(schema = Schema(implementation = SignUpResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<SignUpResponse> {
        val user = userFacade.createUser(request.toData())
        return ResponseEntity.ok(SignUpResponse(user))
    }

    @PostMapping("/signin")
    @Operation(
        summary = "사용자 로그인",
        description = "이메일과 비밀번호로 인증 후 토큰 발급",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 인증 성공",
                content = [Content(schema = Schema(implementation = SignInResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "잘못된 인증 정보",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
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
