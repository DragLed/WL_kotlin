package com.wishlistApp.service

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository

class UserService(
    private val userRepository: UserRepository,
) {
    fun create(username: String, password: String): User {
        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
        require(password.isNotBlank()) { "Пароль не может быть пустым" }
        require(password.length >= 8) { "Пароль должен содержать не менее 8 символов" }

        return userRepository.create(User(0, username, password))
    }

    fun getAll(): List<User> {
        return userRepository.findAll()
    }

    fun getById(id: Int): User? {
        return userRepository.findById(id)
    }

    fun updatename(id: Int, username: String): Boolean {
        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
        return userRepository.updatename(id, username)
    }

    fun updatepassword(id: Int, password: String): Boolean {
        require(password.isNotBlank()) { "Пароль не может быть пустым" }
        require(password.length >= 8) { "Пароль должен содержать не менее 8 символов" }
        return userRepository.updatepassword(id, password)
    }

    fun delete(id: Int): Boolean {
        return userRepository.delete(id)
    }
}
