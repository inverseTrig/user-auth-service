package com.example.userauthservice

import io.kotest.core.config.AbstractProjectConfig

class KotestConfiguration : AbstractProjectConfig() {
    override val globalAssertSoftly: Boolean = true
}
