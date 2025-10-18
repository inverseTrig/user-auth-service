package com.example.userauthservice

import com.example.userauthservice.application.configuration.security.JwtTokenProcessor
import com.example.userauthservice.application.configuration.security.TokenPrincipal
import com.example.userauthservice.application.facade.UserFacade
import com.example.userauthservice.domain.refreshToken.RefreshToken
import com.example.userauthservice.domain.refreshToken.RefreshTokenRepository
import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.User
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.ApplicationContext
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.reflect.KClass

@TestComponent
class TestHelper(
    private val context: ApplicationContext,
) {
    fun getUser(id: Long): User {
        val facade = UserFacade::class.getBean()
        return facade.getUserById(id)
    }

    fun passwordMatches(
        password: String,
        expected: String,
    ): Boolean {
        val passwordEncoder = PasswordEncoder::class.getBean()
        return passwordEncoder.matches(expected, password)
    }

    fun createUser(
        name: String = generateString(),
        email: String = generateEmail(),
        password: String = generateString(),
        role: com.example.userauthservice.domain.user.Role = com.example.userauthservice.domain.user.Role.MEMBER,
    ): User {
        val facade = UserFacade::class.getBean()

        val data =
            CreateUserData(
                name = name,
                email = email,
                password = password,
                role = role,
            )

        return facade.createUser(data)
    }

    fun validateToken(
        token: String,
        assertions: (principal: TokenPrincipal) -> Unit,
    ) {
        val jwtTokenProcessor = JwtTokenProcessor::class.getBean()
        val principal = jwtTokenProcessor.getPrincipal(token)
        assertions(principal)
    }

    fun getRefreshToken(refreshToken: String): RefreshToken {
        val repository = RefreshTokenRepository::class.getBean()
        return repository.findAll().first { it.token == refreshToken }
    }

    fun generateToken(user: User): String {
        val jwtTokenProcessor = JwtTokenProcessor::class.getBean()
        return jwtTokenProcessor.generateAccessToken(user)
    }

    private fun <T : Any> KClass<T>.getBean(): T = context.getBean(this.java)
}
