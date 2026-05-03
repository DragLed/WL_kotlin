package com.wishlistApp.repository

import com.wishlistApp.model.Wishlist


interface WishlistRepository {
    fun create(wishlist: Wishlist): Wishlist
    // fun findById(id: Int): Wishlist?
    fun findAll(): List<Wishlist>
    // fun update(id: Int, wishlist: Wishlist): Boolean
    // fun delete(id: Int): Boolean
}
