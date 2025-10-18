package com.example.userauthservice.application.presentation.controller.user

import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.application.presentation.controller.docs.GetUserById
import com.example.userauthservice.application.presentation.controller.docs.PutUser
import com.example.userauthservice.application.presentation.dto.UpdateUserRequest
import com.example.userauthservice.application.presentation.dto.UserResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "사용자 관리 API")
class UserController(
    private val userFacade: UserFacade,
) {
    @GetUserById
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #id == authentication.principal.userId)")
    fun getById(
        @PathVariable id: Long,
    ): ResponseEntity<UserResponse> {
        val user = userFacade.getUserById(id)
        return ResponseEntity.ok(UserResponse(user))
    }

    @PutUser
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #id == authentication.principal.userId)")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest,
    ): ResponseEntity<UserResponse> {
        val user = userFacade.updateUser(request.toData(id))
        return ResponseEntity.ok(UserResponse(user))
    }
}
