package com.wishlistApp.service

import com.wishlistApp.core.JwtConfig
import com.wishlistApp.dto.LoginRequest
import com.wishlistApp.dto.TokenResponse
import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository

class UserService(
    private val userRepository: UserRepository,
) {

    object PasswordUtil {

        fun hash(password: String): String {
            return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt())
        }

        fun verify(password: String, hash: String): Boolean {
            return org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)
        }
    }


    fun create(username: String, password: String): User {

        require(username.isNotBlank()) { "Имя пользователя не может быть пустым" }
        require(password.length >= 8) { "Пароль должен содержать не менее 8 символов" }

        val existing = userRepository.findByUsername(username)
        require(existing == null) { "Пользователь уже существует" }

        val hashedPassword = PasswordUtil.hash(password)

        return userRepository.create(User(0, username, hashedPassword))
    }

    fun login(username: String, password: String): TokenResponse {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("Пользователь не найден")

        val valid = PasswordUtil.verify(password, user.password)
        if (!valid) {
            throw IllegalArgumentException("Неверный пароль")
        }
        return TokenResponse(
            accessToken = JwtConfig.generateAccessToken(user.id),
            refreshToken = JwtConfig.generateRefreshToken(user.id)
        )
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
