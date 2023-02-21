package ru.netology.nmedia.auth

data class AuthState(
    val id: Long = 0,
    val token: String? = null
)