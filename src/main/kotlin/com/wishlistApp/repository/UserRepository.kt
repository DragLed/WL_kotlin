package com.wishlistApp.repository

import com.wishlistApp.model.User

interface UserRepository {
    fun create(user: User): User
    fun findById(id: Int): User?
    fun findAll(): List<User>
//    fun update(user: User): Boolean
    fun delete(id: Int): Boolean
}
