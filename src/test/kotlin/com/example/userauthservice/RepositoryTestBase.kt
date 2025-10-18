package com.example.userauthservice

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfiguration::class)
abstract class RepositoryTestBase : FunSpec() {
    override val extensions: List<Extension> = listOf(SpringExtension())

    @Autowired
    protected lateinit var entityManager: TestEntityManager
}

fun TestEntityManager.persistAll(vararg entities: Any): List<Any> =
    entities.map {
        when (it) {
            is Iterable<*> -> it.filterNotNull().map { element -> persistAll(element) }
            else -> persist(it)
        }
    }
