package com.example.userauthservice.application.presentation.controller.docs

import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.SignUpResponse
import com.example.userauthservice.application.presentation.dto.UserTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
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
annotation class PostSignIn

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "사용자 로그인",
    description = "이메일과 비밀번호로 인증 후 토큰 발급",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "사용자 인증 성공",
            content = [Content(schema = Schema(implementation = UserTokenResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "잘못된 인증 정보",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    ],
)
annotation class PostSignUp

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "토큰 갱신",
    description = "Refresh 토큰으로 새로운 Access 토큰과 Refresh 토큰 발급. 토큰 재사용 감지 시 토큰 패밀리 전체 무효화.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = [Content(schema = Schema(implementation = UserTokenResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "유효하지 않거나 재사용된 토큰",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    ],
)
annotation class PostRefreshToken
