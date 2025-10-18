package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidCredentialsException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Transactional
    fun update(data: UpdateUserServiceData): User {
        val user = getById(data.id)

        data.email?.let { newEmail ->
            if (newEmail != user.email && userRepository.existsByEmailAndIdNot(newEmail, data.id)) {
                throw IllegalArgumentException(ErrorMessage.INVALID.EMAIL_ALREADY_EXISTS.message)
            }
        }

        user.update(data.toData())

        return user
    }

    fun getById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow {
                NoSuchElementException(ErrorMessage.NOT_FOUND.USER.message)
            }
    }

    fun getUsersByPage(
        filter: UserFilter,
        pageable: Pageable,
    ): Page<User> {
        return userRepository.findAll(filter, pageable)
    }

    @Transactional
    fun deleteById(id: Long) {
        val user = getById(id)
        user.softDelete()
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

data class UpdateUserServiceData(
    val id: Long,
    val name: String?,
    val email: String?,
) {
    fun toData(): UpdateUserData =
        UpdateUserData(
            name = this.name,
            email = this.email,
        )
}
