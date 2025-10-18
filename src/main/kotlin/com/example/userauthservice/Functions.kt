package com.example.userauthservice

fun String.isValidEmail() =
    this.matches(
        Regex("^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"),
    )
