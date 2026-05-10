package com.wishlistApp.service

import com.wishlistApp.core.WishlistVisibility
import com.wishlistApp.model.Wishlist
import com.wishlistApp.repository.WishlistRepository

class WishlistService(
    private val wishlistRepository: WishlistRepository,
    private val accessService: WishlistAccessService = WishlistAccessService()
) {

    fun create(
        userId: Int,
        title: String,
        description: String,
        visibility: WishlistVisibility
    ): Wishlist {
        require(userId > 0) { "ID пользователя должен быть положительным числом" }
        require(title.isNotBlank()) { "Название не может быть пустым" }
        require(description.isNotBlank()) { "Описание не может быть пустым" }

        val wishlist = Wishlist(
            id = 0,
            userId = userId,
            title = title,
            description = description,
            createdAt = "",
            visibility = visibility
        )

        return wishlistRepository.create(wishlist)
    }

    fun getAll(): List<Wishlist> {
        return wishlistRepository.findAll()
    }

    fun getById(currentUserId: Int, wishlistId: Int): Wishlist? {
        val wishlist = wishlistRepository.findById(currentUserId, wishlistId)
            ?: return null

        return if (accessService.canView(wishlist, currentUserId)) {
            wishlist
        } else {
            null
        }
    }

    fun update(
        wishlistId: Int,
        currentUserId: Int,
        title: String,
        description: String,
        visibility: WishlistVisibility
    ): Boolean {
        require(title.isNotBlank()) { "Название не может быть пустым" }
        require(description.isNotBlank()) { "Описание не может быть пустым" }

        val existingWishlist =
            wishlistRepository.findById(currentUserId, wishlistId)
                ?: return false

        if (!accessService.canEdit(existingWishlist, currentUserId)) {
            return false
        }

        val updatedWishlist = existingWishlist.copy(
            title = title,
            description = description,
            visibility = visibility
        )

        return wishlistRepository.update(wishlistId, updatedWishlist)
    }

    fun delete(wishlistId: Int, currentUserId: Int): Boolean {
        val wishlist =
            wishlistRepository.findById(currentUserId, wishlistId)
                ?: return false

        if (!accessService.canDelete(wishlist, currentUserId)) {
            return false
        }

        return wishlistRepository.delete(wishlistId)
    }
}