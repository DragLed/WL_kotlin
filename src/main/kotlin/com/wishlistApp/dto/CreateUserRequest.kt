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
data class UserResponse(
    val id: Int,
    val username: String
)
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)