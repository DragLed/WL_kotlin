package com.wishlistApp.routing

import com.wishlistApp.model.Gift
import com.wishlistApp.service.GiftService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.text.toIntOrNull

fun Route.giftRoute(giftService: GiftService) {

    post("/gift") {
        try {
            val gift = call.receive<Gift>()
            val createdGift = giftService.create(gift)

            call.respond(HttpStatusCode.Created, createdGift)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/gifts") {
        try {
            val gifts = giftService.findAll()
            call.respond(HttpStatusCode.OK, gifts)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Internal server error"))
            )
        }
    }

    get("/gift/{id}") {
        try {
            val id = call.requirePositiveId()
            val gift = giftService.findById(id)

            if (gift != null) {
                call.respond(HttpStatusCode.OK, gift)
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

    delete("/gift/{id}") {
        try {
            val id = call.requirePositiveId()
            val deleted = giftService.delete(id)

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