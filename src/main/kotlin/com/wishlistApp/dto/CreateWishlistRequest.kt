package com.wishlistApp.dto

import kotlinx.serialization.Serializable
import com.wishlistApp.core.WishlistVisibility

@Serializable
data class CreateWishlistRequest(
    val userId: Int,
    val title: String,
    val description: String,
    val visibility: WishlistVisibility
)
