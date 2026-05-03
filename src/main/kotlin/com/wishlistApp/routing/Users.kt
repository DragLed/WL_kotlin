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
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.text.toIntOrNull

fun Route.userRoute(UserService: UserService) {


    post("/user") {

        val request = call.receive<CreateUserRequest>()

        try {
            val user = UserService.create(

                request.username,
                request.password
            )

            call.respond(HttpStatusCode.Created, UserResponse(user.id, request.username))
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    post("/login") {
        val request = call.receive<LoginRequest>()

        try {
            val tokens = UserService.login(request.username, request.password)
            call.response.cookies.append(
                Cookie(
                    name = "refresh_token",
                    value = tokens.refreshToken,
                    httpOnly = true,
                    secure = false,
                    path = "/",
                    maxAge = 7 * 24 * 60 * 60
                )
            )

            call.respond(tokens.accessToken)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    post("/logout") {
        call.response.cookies.append(
            Cookie(
                name = "refresh_token",
                value = "",
                maxAge = 0,
                path = "/"
            )
        )

        call.respond(HttpStatusCode.OK, "Successfully logged out")
    }

    post("/refresh") {

        val refreshToken = call.request.cookies["refresh_token"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        try {
            val decoded = JwtConfig.verifier().verify(refreshToken)
            val userId = decoded.getClaim("userId").asInt()

            val newAccess = JwtConfig.generateAccessToken(userId)

            call.respond(newAccess)


        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
    authenticate("auth-jwt") {
        get("/me") {
            val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt()

            call.respond(mapOf("userId" to userId))
        }
    }

    get("/users") {
        try {
            val users = UserService.getAll()
            call.respond(HttpStatusCode.OK, users)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/user/{id}") {
        try {
            val id = call.requirePositiveId()
            val user = UserService.getById(id)

            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    put("/user/{id}") {
        try {
            val id = call.requirePositiveId()
            val request = call.receive<ChangeUsernameRequest>()
            val updated = UserService.updatename(id, request.username)

            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("updated" to true))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    put("/user/password/{id}") {
        try {
            val id = call.requirePositiveId()
            val request = call.receive<ChangePasswordRequest>()
            val updated = UserService.updatepassword(id, request.password)

            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("updated" to true))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    delete("/user/{id}") {
        try {
            val id = call.requirePositiveId()
            val deleted = UserService.delete(id)

            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }
}

private fun ApplicationCall.requirePositiveId(): Int {
    val id = parameters["id"]?.toIntOrNull()
    require(id != null && id > 0) {
        "Некорректный id. Используй положительное целое число."
    }
    return id
}


