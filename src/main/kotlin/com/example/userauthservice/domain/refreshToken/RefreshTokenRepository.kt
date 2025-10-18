package com.example.userauthservice.domain.refreshToken

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long>
