package com.wishlistApp.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWishlistRequest(
    val userId: Int,
    val title: String,
    val description: String,
    val visibility: String
)
