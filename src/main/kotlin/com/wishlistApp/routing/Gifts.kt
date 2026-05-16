package com.wishlistApp.routing

import com.wishlistApp.dto.MessageResponse
import com.wishlistApp.model.Gift
import com.wishlistApp.service.GiftService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.text.toIntOrNull
import com.wishlistApp.exception.UnauthorizedException
import io.ktor.server.auth.authenticate

fun Route.giftRoute(giftService: GiftService) {

    post("/gift") {
        try {
            val gift = call.receive<Gift>()
            val createdGift = giftService.create(gift)

            call.respond(HttpStatusCode.Created, createdGift)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.OK,
                MessageResponse(
                    success = true,
                    message = "Подарок успешно зарезервирован"
                )
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
                HttpStatusCode.OK,
                MessageResponse(
                    success = true,
                    message = "Подарок успешно зарезервирован"
                )
            )
        }
    }


    authenticate("auth-jwt") {

    post("/gift/{id}/reserve") {

        val principal = call.principal<JWTPrincipal>()
            ?: throw UnauthorizedException("Требуется авторизация")

        val userId = principal.payload
            .getClaim("userId")
            .asInt()
            ?: throw UnauthorizedException("Некорректный токен")

        val giftId = call.parameters["id"]?.toIntOrNull()
            ?: throw BadRequestException("Некорректный ID подарка")

        giftService.reserve(giftId, userId)

        call.respond(
            HttpStatusCode.OK,
            MessageResponse(
                success = true,
                message = "Подарок успешно зарезервирован"
            )
        )
    }

    post("/gift/{id}/unreserve") {
        val principal = call.principal<JWTPrincipal>()
            ?: throw UnauthorizedException("Требуется авторизация")

        val userId = principal.payload
            .getClaim("userId")
            .asInt()
            ?: throw UnauthorizedException("Некорректный токен")

        val giftId = call.parameters["id"]?.toIntOrNull()
            ?: throw BadRequestException("Некорректный ID подарка")

        giftService.unreserve(giftId, userId)

        call.respond(
            HttpStatusCode.OK,
            MessageResponse(
                success = true,
                message = "Подарок успешно разрезервирован"
            )
        )

    }

    delete("/gift/{id}") {
        try {
            val principal = call.principal<JWTPrincipal>()
                ?: throw UnauthorizedException("Требуется авторизация")

            val userId = principal.payload
                .getClaim("userId")
                .asInt()
                ?: throw UnauthorizedException("Некорректный токен")

            val giftId = call.requirePositiveId()
            val deleted = giftService.delete(giftId, userId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.OK,
                MessageResponse(
                    success = true,
                    message = "Подарок успешно зарезервирован"
                )
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