package com.example.userauthservice.application.presentation

import com.example.userauthservice.InvalidCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = HttpStatus.UNAUTHORIZED.reasonPhrase,
                    message = ex.message ?: "Invalid credentials",
                    path = request.getDescription(false).removePrefix("uri="),
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = errorMessage ?: "Validation failed",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = ex.message ?: "Invalid argument",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = ex.message ?: "An unexpected error occurred",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)
