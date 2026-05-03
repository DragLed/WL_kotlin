package com.wishlistApp.core

import kotlinx.serialization.Serializable

@Serializable
enum class WishlistVisibility {
    public,
    link_only,
    private
}