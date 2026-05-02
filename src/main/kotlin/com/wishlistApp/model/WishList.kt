package com.wishlistApp.model

import kotlinx.serialization.Serializable
import com.wishlistApp.core.WishlistVisibility

@Serializable
data class Wishlist(
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String,
//    val createdAt: String?,
    val visibility: WishlistVisibility
)