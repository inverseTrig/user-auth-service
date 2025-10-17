package com.example.userauthservice

fun generateString(length: Int = 20): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}

fun generateEmail(): String {
    return "${generateString(10)}@${generateString(5)}.com"
}
