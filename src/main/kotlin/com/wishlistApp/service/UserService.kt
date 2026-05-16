package com.wishlistApp.service

import com.wishlistApp.core.JwtConfig
import com.wishlistApp.dto.TokenResponse
import com.wishlistApp.exception.BadRequestException
import com.wishlistApp.exception.ForbiddenException
import io.ktor.server.plugins.NotFoundException
import com.wishlistApp.exception.UnauthorizedException
import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

class UserService(
    private val userRepository: UserRepository,
) {

    object PasswordUtil {

        /**
         * Хеширование пароля.
         */
        fun hash(password: String): String {
            return BCrypt.hashpw(password, BCrypt.gensalt())
        }

        /**
         * Проверка пароля.
         */
        fun verify(password: String, hash: String): Boolean {
            return BCrypt.checkpw(password, hash)
        }
    }

    /**
     * Регистрация пользователя.
     *
     * Возможные ошибки:
     * - 400 Bad Request -> некорректные данные
     */
    fun create(username: String, password: String): User {

        if (username.isBlank()) {
            throw BadRequestException("Имя пользователя не может быть пустым")
        }

        if (password.length < 8) {
            throw BadRequestException("Пароль должен содержать не менее 8 символов")
        }

        val existing = userRepository.findByUsername(username)
        if (existing != null) {
            throw BadRequestException("Пользователь с таким именем уже существует")
        }

        val hashedPassword = PasswordUtil.hash(password)

        return userRepository.create(
            User(
                id = 0,
                username = username,
                password = hashedPassword,
                createdAt = "",

            )
        )
    }

    /**
     * Авторизация пользователя.
     *
     * Возможные ошибки:
     * - 401 Unauthorized -> пользователь не найден или пароль неверный
     */
    fun login(username: String, password: String): TokenResponse {
        val user = userRepository.findByUsername(username)
            ?: throw UnauthorizedException("Неверный логин или пароль")

        val valid = PasswordUtil.verify(password, user.password)

        if (!valid) {
            throw UnauthorizedException("Неверный логин или пароль")
        }

        return TokenResponse(
            accessToken = JwtConfig.generateAccessToken(user.id),
            refreshToken = JwtConfig.generateRefreshToken(user.id)
        )
    }

    /**
     * Получить список всех пользователей.
     */
    fun getAll(): List<User> {
        return userRepository.findAll()
    }

    /**
     * Получить пользователя по ID.
     *
     * Возможные ошибки:
     * - 404 Not Found -> пользователь не найден
     */
    fun getById(id: Int): User {
        return userRepository.findById(id)
            ?: throw NotFoundException("Пользователь с id=$id не найден")
    }

    /**
     * Изменение имени пользователя.
     *
     * Возможные ошибки:
     * - 400 Bad Request -> пустое имя
     * - 404 Not Found -> пользователь не найден
     * - 403 Forbidden -> попытка изменить чужой профиль
     */
    fun updatename(id: Int, username: String, currentUserId: Int): Boolean {
        if (username.isBlank()) {
            throw BadRequestException("Имя пользователя не может быть пустым")
        }

        val updated = userRepository.updatename(id, username, currentUserId)

        when {
            updated -> return true
            userRepository.findById(id) == null ->
                throw NotFoundException("Пользователь с id=$id не найден")
            else ->
                throw ForbiddenException("Вы не можете изменить чужой профиль")
        }
    }

    /**
     * Изменение пароля.
     *
     * Возможные ошибки:
     * - 400 Bad Request -> некорректный пароль
     * - 404 Not Found -> пользователь не найден
     * - 403 Forbidden -> попытка изменить чужой профиль
     */
    fun updatepassword(id: Int, password: String, currentUserId: Int): Boolean {
        if (password.isBlank()) {
            throw BadRequestException("Пароль не может быть пустым")
        }

        if (password.length < 8) {
            throw BadRequestException("Пароль должен содержать не менее 8 символов")
        }

        val hashedPassword = PasswordUtil.hash(password)

        val updated = userRepository.updatepassword(
            id = id,
            password = hashedPassword,
            currentUserId = currentUserId
        )

        when {
            updated -> return true
            userRepository.findById(id) == null ->
                throw NotFoundException("Пользователь с id=$id не найден")
            else ->
                throw ForbiddenException("Вы не можете изменить чужой профиль")
        }
    }

    /**
     * Удаление пользователя.
     *
     * Возможные ошибки:
     * - 404 Not Found -> пользователь не найден
     * - 403 Forbidden -> попытка удалить чужой аккаунт
     */
    fun delete(id: Int, currentUserId: Int): Boolean {
        val deleted = userRepository.delete(id, currentUserId)

        when {
            deleted -> return true
            userRepository.findById(id) == null ->
                throw NotFoundException("Пользователь с id=$id не найден")
            else ->
                throw ForbiddenException("Вы не можете удалить чужой аккаунт")
        }
    }
}