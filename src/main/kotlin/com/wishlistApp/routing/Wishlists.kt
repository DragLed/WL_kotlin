package com.wishlistApp.routing

import com.wishlistApp.dto.CreateWishlistRequest
import com.wishlistApp.service.WishlistService
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
import kotlin.text.toIntOrNull

fun Route.wishlistRoute(wishlistService: WishlistService) {

    authenticate("auth-jwt") {

        post("/wishlist") {
            try {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val request = call.receive<CreateWishlistRequest>()

                val wishlist = wishlistService.create(
                    userId = userId,
                    title = request.title,
                    description = request.description,
                    visibility = request.visibility
                )

                call.respond(HttpStatusCode.Created, wishlist)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Bad request"))
                )
            }
        }

        get("/wishlist/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val wishlistId = call.requirePositiveId()

                val wishlist = wishlistService.getById(
                    currentUserId = userId,
                    wishlistId = wishlistId
                )

                if (wishlist != null) {
                    call.respond(HttpStatusCode.OK, wishlist)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Wishlist not found or access denied")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Bad request"))
                )
            }
        }

        delete("/wishlist/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val wishlistId = call.requirePositiveId()

                val deleted = wishlistService.delete(
                    wishlistId = wishlistId,
                    currentUserId = userId
                )

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("deleted" to true)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Access denied or wishlist not found")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Bad request"))
                )
            }
        }
    }

    get("/wishlists") {
        try {
            val wishlists = wishlistService.getAll()
            call.respond(HttpStatusCode.OK, wishlists)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Internal server error"))
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