package com.wishlistApp.service

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository

class UserService(
    private val user: UserRepository,

) {
    fun addUser (
        username: String,
        password: String
    ): User {
        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
        require(password.isNotBlank()) { "Пароль не может быть пустым" }
        require(password.length >= 8) { "Пароль должен содержать не менее 8 символов" }

        return user.post(User(0, username, password, "lol"))
    }
}