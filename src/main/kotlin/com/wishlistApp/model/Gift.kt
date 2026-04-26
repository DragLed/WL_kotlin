package com.wishlistApp.model

import kotlinx.serialization.Serializable

@Serializable
data class Gift(
    val id: Int,
    val WishListId: Int,
    val title: String,
    val description: String,
    val price: Float,  
    val is_reserved: Boolean,
    val reserved_by: Int,
    val createdAt: String,
)
