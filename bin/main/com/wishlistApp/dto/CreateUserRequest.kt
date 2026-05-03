package com.wishlistApp.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val username: String,
    val password: String
)

@Serializable
data class ChangeUsernameRequest(
    val username: String
)

@Serializable
data class ChangePasswordRequest(
    val password: String
)


@Serializable
data class LoginResponse(
    val token: String
)
