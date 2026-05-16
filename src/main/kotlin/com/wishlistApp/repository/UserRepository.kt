package com.wishlistApp.repository

import com.wishlistApp.model.User

interface UserRepository {
    fun findByUsername(username: String): User?
    fun create(user: User): User
    fun findById(id: Int): User?
    fun findAll(): List<User>
    fun updatename(id: Int, username: String, currentUserId: Int): Boolean
    fun updatepassword(id: Int, password: String, currentUserId: Int): Boolean
    fun delete(id: Int, currentUserId: Int): Boolean
}
