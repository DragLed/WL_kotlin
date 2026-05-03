package com.wishlistApp.routing

import com.wishlistApp.dto.CreateWishlistRequest
import com.wishlistApp.service.WishlistService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.text.toIntOrNull

fun Route.wishlistRoute( WishlistService: WishlistService) {



    authenticate("auth-jwt") {

        post("/wishlist") {

            val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt()

            val request = call.receive<CreateWishlistRequest>()
            try {
                val wishlist = WishlistService.create(
                    userId,
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
    }


    get("/wishlists") {
        try {
            val wishlists = WishlistService.getAll()
            call.respond(HttpStatusCode.OK, wishlists)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }
    authenticate("auth-jwt") {
        get("/wishlist/{id}") {
            try {
                val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()

                val wishlist_id = call.requirePositiveId()
                val wishlist = WishlistService.getById(wishlist_id, userId)

                if (wishlist != null) {
                    call.respond(HttpStatusCode.OK, wishlist)
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
}

private fun ApplicationCall.requirePositiveId(): Int {
    val id = parameters["id"]?.toIntOrNull()
    require(id != null && id > 0) {
        "Некорректный id. Используй положительное целое число."
    }
    return id
}