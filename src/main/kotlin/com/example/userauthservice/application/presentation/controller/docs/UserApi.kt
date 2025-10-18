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

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "사용자 정보 수정",
    description = "사용자 ID로 사용자 정보를 수정합니다. 인증이 필요하며, ADMIN 권한이거나 본인만 수정할 수 있습니다.",
)
@ApiResponse(
    responseCode = "200",
    description = "사용자 정보 수정 성공",
    content = [Content(schema = Schema(implementation = UserResponse::class))],
)
@ApiResponse(
    responseCode = "400",
    description = "잘못된 요청 (유효하지 않은 이메일 형식 등)",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
@ApiResponse(
    responseCode = "404",
    description = "사용자를 찾을 수 없음",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
@Parameter(name = "id", description = "수정할 사용자의 ID", required = true)
@SecuredEndpoint
annotation class PutUser

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "사용자 목록 조회",
    description = "모든 사용자 목록을 페이지 단위로 조회합니다. 이름 또는 이메일로 검색할 수 있습니다. ADMIN 권한이 필요합니다.",
)
@ApiResponse(
    responseCode = "200",
    description = "사용자 목록 조회 성공",
    content = [Content(schema = Schema(implementation = UserResponse::class))],
)
@Parameter(name = "name", description = "사용자 이름으로 검색 (부분 일치)", required = false)
@Parameter(name = "email", description = "이메일로 검색 (부분 일치)", required = false)
@Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = false)
@Parameter(name = "size", description = "페이지 크기", required = false)
@Parameter(name = "sort", description = "정렬 기준 (예: name,asc 또는 email,desc)", required = false)
@SecuredEndpoint
annotation class GetUsers

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "사용자 삭제",
    description = "사용자 ID로 사용자를 삭제합니다. 인증이 필요하며, ADMIN 권한이거나 본인만 삭제할 수 있습니다.",
)
@ApiResponse(
    responseCode = "204",
    description = "사용자 삭제 성공",
)
@ApiResponse(
    responseCode = "404",
    description = "사용자를 찾을 수 없음",
    content = [Content(schema = Schema(implementation = ErrorResponse::class))],
)
@Parameter(name = "id", description = "삭제할 사용자의 ID", required = true)
@SecuredEndpoint
annotation class DeleteUser
