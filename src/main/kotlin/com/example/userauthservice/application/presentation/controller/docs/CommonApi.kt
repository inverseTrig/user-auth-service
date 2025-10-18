@file:Suppress("ktlint:standard:filename")

package com.example.userauthservice.application.presentation.controller.docs

import com.example.userauthservice.application.presentation.ErrorResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(
    responseCode = "401",
    description = "인증 실패 - JWT 토큰이 없거나 유효하지 않음",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
@ApiResponse(
    responseCode = "403",
    description = "권한 없음 - 접근 권한이 없습니다",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
annotation class SecuredEndpoint
