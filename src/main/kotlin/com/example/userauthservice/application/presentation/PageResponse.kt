package com.example.userauthservice.application.presentation

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: PageMetadata,
) {
    data class PageMetadata(
        val number: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int,
        val isFirst: Boolean,
        val isLast: Boolean,
    )

    companion object {
        fun <T> of(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page =
                    PageMetadata(
                        number = page.number,
                        size = page.size,
                        totalElements = page.totalElements,
                        totalPages = page.totalPages,
                        isFirst = page.isFirst,
                        isLast = page.isLast,
                    ),
            )
        }
    }
}
