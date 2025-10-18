@file:Suppress("ktlint:standard:filename")

package com.example.userauthservice.application.presentation.controller.docs

import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "사용자 ID로 조회",
    description = "사용자 ID로 사용자 상세 정보를 조회합니다. 인증이 필요합니다.",
)
@ApiResponse(
    responseCode = "200",
    description = "사용자 조회 성공",
    content = [Content(schema = Schema(implementation = UserResponse::class))],
)
@ApiResponse(
    responseCode = "404",
    description = "사용자를 찾을 수 없음",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
@Parameter(name = "id", description = "조회할 사용자의 ID", required = true)
@SecuredEndpoint
annotation class GetUserById
