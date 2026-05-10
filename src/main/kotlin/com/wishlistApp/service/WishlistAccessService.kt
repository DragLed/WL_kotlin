package com.wishlistApp.service

import com.wishlistApp.core.WishlistVisibility
import com.wishlistApp.model.Wishlist

class WishlistAccessService {

    fun canView(wishlist: Wishlist, currentUserId: Int?): Boolean {
        return when (wishlist.visibility) {
            WishlistVisibility.public -> true
            WishlistVisibility.link_only -> true
            WishlistVisibility.private ->
                currentUserId != null && wishlist.userId == currentUserId
        }
    }

    fun canEdit(wishlist: Wishlist, currentUserId: Int?): Boolean {
        return currentUserId != null &&
                wishlist.userId == currentUserId
    }

    fun canDelete(wishlist: Wishlist, currentUserId: Int?): Boolean {
        return canEdit(wishlist, currentUserId)
    }

    fun canReserve(wishlist: Wishlist, currentUserId: Int?): Boolean {
        return currentUserId != null &&
                wishlist.userId != currentUserId
    }
}