package com.wishlistApp.model

import kotlinx.serialization.Serializable

@Serializable
data class WishlistAccess(
    val id: Int,
    val wishlistId: Int,   
    val userId: Int,
    val role: String,  // "owner", "editor", "viewer"
    val createdAt: String
)
