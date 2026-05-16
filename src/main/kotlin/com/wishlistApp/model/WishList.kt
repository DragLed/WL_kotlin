package com.wishlistApp.model

import kotlinx.serialization.Serializable
import com.wishlistApp.core.WishlistVisibility

@Serializable
data class Wishlist(
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String?,
    val visibility: WishlistVisibility,
    val createdAt: String,
    val gifts: List<Gift> = emptyList()
)