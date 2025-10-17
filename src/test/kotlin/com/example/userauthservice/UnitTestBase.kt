package com.example.userauthservice

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.mockk.clearAllMocks

abstract class UnitTestBase : FunSpec() {
    override suspend fun beforeEach(testCase: TestCase) {
        clearAllMocks()
    }
}
