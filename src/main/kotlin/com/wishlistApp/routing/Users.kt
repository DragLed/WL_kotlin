package com.wishlistApp.routing

import com.wishlistApp.core.JwtConfig
import com.wishlistApp.dto.ChangePasswordRequest
import com.wishlistApp.dto.ChangeUsernameRequest
import com.wishlistApp.dto.CreateUserRequest
import com.wishlistApp.dto.LoginRequest
import com.wishlistApp.dto.UserResponse
import com.wishlistApp.service.UserService
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.userRoute(userService: UserService) {

    post("/user") {
        try {
            val request = call.receive<CreateUserRequest>()

            // Валидация входных данных
            if (request.username.isBlank() || request.password.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username and password cannot be empty")
                )
                return@post
            }

            if (request.username.length < 3 || request.username.length > 50) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username must be between 3 and 50 characters")
                )
                return@post
            }

            if (request.password.length < 6) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Password must be at least 6 characters")
                )
                return@post
            }

            val user = userService.create(request.username, request.password)
            call.respond(HttpStatusCode.Created, UserResponse(user.id, request.username))

        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Invalid input data"))
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Failed to create user: ${e.message}")
            )
        }
    }

    post("/login") {
        try {
            val request = call.receive<LoginRequest>()

            // Валидация входных данных
            if (request.username.isBlank() || request.password.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username and password are required")
                )
                return@post
            }

            val tokens = userService.login(request.username, request.password)

            call.response.cookies.append(
                Cookie(
                    name = "refresh_token",
                    value = tokens.refreshToken,
                    httpOnly = true,
                    secure = false, // Установите true для production с HTTPS
                    path = "/",
                    maxAge = 7 * 24 * 60 * 60, // 7 дней
                    domain = null,
                    extensions = emptyMap()
                )
            )

            call.respond(
                HttpStatusCode.OK,
                mapOf("accessToken" to tokens.accessToken)
            )

        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to (e.message ?: "Invalid credentials"))
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Login failed: ${e.message}")
            )
        }
    }

    post("/logout") {
        try {
            call.response.cookies.append(
                Cookie(
                    name = "refresh_token",
                    value = "",
                    httpOnly = true,
                    secure = false,
                    path = "/",
                    maxAge = 0, // Удаляем cookie
                    domain = null,
                    extensions = emptyMap()
                )
            )

            call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully logged out"))

        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Logout failed: ${e.message}")
            )
        }
    }

    post("/refresh") {
        try {
            val refreshToken = call.request.cookies["refresh_token"]

            if (refreshToken.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Refresh token is missing")
                )
                return@post
            }

            val decoded = JwtConfig.verifier().verify(refreshToken)
            val userId = decoded.getClaim("userId").asInt()

            val newAccessToken = JwtConfig.generateAccessToken(userId)

            call.respond(
                HttpStatusCode.OK,
                mapOf("accessToken" to newAccessToken)
            )

        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid or expired refresh token")
            )
        }
    }

    authenticate("auth-jwt") {
        get("/me") {
            try {
                val principal = call.principal<JWTPrincipal>()

                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@get
                }

                val userId = principal.payload.getClaim("userId").asInt()
                val user = userService.getById(userId)

                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }



            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get user info: ${e.message}")
                )
            }
        }

        get("/users") {
            try {
                val users = userService.getAll()
                call.respond(HttpStatusCode.OK, users)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get users: ${e.message}")
                )
            }
        }

        get("/user/{id}") {
            try {
                val id = call.requirePositiveId()
                val user = userService.getById(id)

                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }

            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid user ID"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get user: ${e.message}")
                )
            }
        }

        put("/user/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()

                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@put
                }

                val currentUserId = principal.payload.getClaim("userId").asInt()
                val id = call.requirePositiveId()

                // Проверка прав доступа
                if (currentUserId != id) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "You can only change your own username")
                    )
                    return@put
                }

                val request = call.receive<ChangeUsernameRequest>()

                // Валидация нового username
                if (request.username.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Username cannot be empty")
                    )
                    return@put
                }

                if (request.username.length < 3 || request.username.length > 50) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Username must be between 3 and 50 characters")
                    )
                    return@put
                }

                val updated = userService.updateUsername(id, request.username, currentUserId)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("updated" to true))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }

            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid input data"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update username: ${e.message}")
                )
            }
        }

        put("/user/password/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()

                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@put
                }

                val currentUserId = principal.payload.getClaim("userId").asInt()
                val id = call.requirePositiveId()

                // Проверка прав доступа
                if (currentUserId != id) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "You can only change your own password")
                    )
                    return@put
                }

                val request = call.receive<ChangePasswordRequest>()

                // Валидация пароля
                if (request.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Password cannot be empty")
                    )
                    return@put
                }

                if (request.password.length < 6) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Password must be at least 6 characters")
                    )
                    return@put
                }

                val updated = userService.updatePassword(id, request.password, currentUserId)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("updated" to true))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }

            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid input data"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update password: ${e.message}")
                )
            }
        }

        delete("/user/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()

                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@delete
                }

                val currentUserId = principal.payload.getClaim("userId").asInt()
                val id = call.requirePositiveId()

                // Проверка прав доступа
                if (currentUserId != id) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "You can only delete your own account")
                    )
                    return@delete
                }

                val deleted = userService.delete(id, currentUserId)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }

            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid user ID"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to delete user: ${e.message}")
                )
            }
        }
    }
}

private fun ApplicationCall.requirePositiveId(): Int {
    val id = parameters["id"]?.toIntOrNull()
    require(id != null && id > 0) {
        "Invalid ID format. Please provide a positive integer."
    }
    return id
}