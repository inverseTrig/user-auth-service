package com.example.userauthservice.domain.refreshToken

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class RefreshTokenSecurityService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun revokeTokenFamily(familyId: Long) {
        val tokensInFamily = refreshTokenRepository.findAllByFamilyId(familyId)

        tokensInFamily.forEach {
            it.revoke()
        }
    }
}
