package com.wishlistApp.service

import com.wishlistApp.core.WishlistVisibility
import com.wishlistApp.model.Wishlist
import com.wishlistApp.repository.WishlistRepository

class WishlistService(
    private val WishlistRepository: WishlistRepository,
) {
    fun create (
        userId: Int,
        title: String,
        description: String,
        visibility: WishlistVisibility
    ): Wishlist {
        require(userId > 0) { "ID пользователя не может быть нулевым" }
        require(title.isNotBlank()) { "Название не может быть пустым" }
        require(description.isNotBlank()) { "Описание не может быть пустым" }

        return WishlistRepository.create(Wishlist(0, userId, title, description, visibility ))
    }

     fun getAll(): List<Wishlist> {
         return WishlistRepository.findAll()
     }

     fun getById(user_id:Int, wishlist_id: Int): Wishlist? {
          return WishlistRepository.findById(user_id, wishlist_id)
     }

//    fun update (
//        username: String
//    ): User {
//        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
//
//        return UserRepository.update(User(0, username))
//    }
//
//     fun delete(id: Int): Boolean {
//         return UserRepository.delete(id)
//     }
}