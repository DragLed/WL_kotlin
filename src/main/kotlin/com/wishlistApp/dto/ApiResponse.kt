package com.wishlistApp.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class DeleteResponse(
    val deleted: Boolean
)