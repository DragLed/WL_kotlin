package com.wishlistApp.service

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository

class UserService(
    private val UserRepository: UserRepository,

) {
    fun create (
        username: String,
        password: String
    ): User {
        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
        require(password.isNotBlank()) { "Пароль не может быть пустым" }
        require(password.length >= 8) { "Пароль должен содержать не менее 8 символов" }

        return UserRepository.create(User(0, username, password))
    }

    fun getAll(): List<User> {
        return UserRepository.findAll()
    }

    fun getById(id: Int): User? {
         return UserRepository.findById(id)
    }

//    fun update (
//        username: String
//    ): User {
//        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
//
//        return UserRepository.update(User(0, username))
//    }
//
    fun delete(id: Int): Boolean {
        return UserRepository.delete(id)
    }
}