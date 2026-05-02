package com.wishlistApp

import com.wishlistApp.dto.ChangePasswordRequest
import com.wishlistApp.dto.ChangeUsernameRequest
import com.wishlistApp.dto.CreateUserRequest
import com.wishlistApp.dto.CreateWishlistRequest
import com.wishlistApp.service.UserService
import com.wishlistApp.service.WishlistService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureRoutes(userService: UserService, wishlistService: WishlistService) {
    post("/users") {
        val request = call.receive<CreateUserRequest>()
        try {
            val user = userService.create(

                request.username, 
                request.password)

            call.respond(HttpStatusCode.Created, user)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/users") {
        try {
            val users = userService.getAll()
            call.respond(HttpStatusCode.OK, users)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/users/{id}") {
        try {
            val id = call.requirePositiveId()
            val user = userService.getById(id)

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

    put("/users/{id}") {
        try {
            val id = call.requirePositiveId()
            val request = call.receive<ChangeUsernameRequest>()
            val updated = userService.updatename(id, request.username)

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

    put("/users/password/{id}") {
        try {
            val id = call.requirePositiveId()
            val request = call.receive<ChangePasswordRequest>()
            val updated = userService.updatepassword(id, request.password)

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

    delete("/users/{id}") {
        try {
            val id = call.requirePositiveId()
            val deleted = userService.delete(id)

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

    post("/wishlists") {
        val request = call.receive<CreateWishlistRequest>()
        try {
            val wishlist = wishlistService.create(
                request.userId,
                request.title,
                request.description,
                request.visibility
            )

            call.respond(HttpStatusCode.Created, wishlist)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/wishlists") {
        try {
            val wishlists = wishlistService.getAll()
            call.respond(HttpStatusCode.OK, wishlists)
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
