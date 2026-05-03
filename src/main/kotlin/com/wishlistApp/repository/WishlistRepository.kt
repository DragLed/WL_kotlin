package com.wishlistApp.repository

import com.wishlistApp.model.Wishlist


interface WishlistRepository {
    fun create(wishlist: Wishlist): Wishlist
    fun findById(user_id:Int, wishlist_id: Int): Wishlist?
    fun findAll(): List<Wishlist>
    // fun update(id: Int, wishlist: Wishlist): Boolean
    // fun delete(id: Int): Boolean
}
