package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidCredentialsException
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun create(data: CreateUserData): User {
        require(!userRepository.existsByEmail(data.email)) {
            ErrorMessage.INVALID.EMAIL_ALREADY_EXISTS.message
        }

        val encryptedPassword = passwordEncoder.encode(data.password)
        val user = data.toEntity(encryptedPassword)

        return userRepository.save(user)
    }

    fun authenticate(
        email: String,
        password: String,
    ): User {
        val user =
            userRepository.findByEmail(email)
                ?: throw InvalidCredentialsException(ErrorMessage.INVALID.INVALID_CREDENTIALS.message)

        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException(ErrorMessage.INVALID.INVALID_CREDENTIALS.message)
        }

        return user
    }

    fun getById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow {
                NoSuchElementException(ErrorMessage.NOT_FOUND.USER.message)
            }
    }
}

data class CreateUserData(
    val name: String,
    val email: String,
    val password: String,
    val role: Role = Role.MEMBER,
) {
    fun toEntity(encryptedPassword: String): User =
        User(
            name = this.name,
            email = this.email,
            password = encryptedPassword,
            role = this.role,
        )
}
