package com.example.userauthservice.domain.user

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun existsByEmailAndIdNot(
        email: String,
        id: Long,
    ): Boolean
}

data class UserFilter(
    val name: String? = null,
    val email: String? = null,
) : Specification<User> {
    override fun toPredicate(
        root: Root<User?>,
        query: CriteriaQuery<*>?,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate {
        val predicates = mutableListOf<Predicate>()

        name?.let {
            if (it.isNotBlank()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%${it.lowercase()}%",
                    ),
                )
            }
        }

        email?.let {
            if (it.isNotBlank()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%${it.lowercase()}%",
                    ),
                )
            }
        }

        return criteriaBuilder.and(*predicates.toTypedArray())
    }
}
