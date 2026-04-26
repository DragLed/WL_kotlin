package com.wishlistApp.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGiftRequest(
    val wishlistId: Int,
    val title: String,
    val description: String,
    val price: Float
)
