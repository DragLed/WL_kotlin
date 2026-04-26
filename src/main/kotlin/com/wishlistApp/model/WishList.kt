package com.wishlistApp.model

import kotlinx.serialization.Serializable

@Serializable
data class Wishlist(
    val id: Int,
    val userId: Int,  
    val title: String,
    val description: String,
    val createdAt: String,
    val visibility: String,
)
