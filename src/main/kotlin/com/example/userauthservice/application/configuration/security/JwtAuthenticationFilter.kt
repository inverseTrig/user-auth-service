package com.example.userauthservice.application.configuration.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProcessor: JwtTokenProcessor,
) : OncePerRequestFilter() {
    private val logger = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null) {
                val principal = jwtTokenProcessor.getPrincipal(token)

                if (principal.tokenType == TokenType.ACCESS.name) {
                    val role = if (principal.role.startsWith("ROLE_")) principal.role else "ROLE_${principal.role}"
                    val authorities = listOf(SimpleGrantedAuthority(role))

                    val authentication =
                        UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities,
                        )

                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication

                    logger.debug { "Successfully authenticated user ${principal.userId} with role ${principal.role}" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to authenticate user from JWT token: ${e.message}" }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
