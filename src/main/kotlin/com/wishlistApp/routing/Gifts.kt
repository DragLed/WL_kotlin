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
import com.wishlistApp.exception.UnauthorizedException
import io.ktor.server.auth.authenticate

fun Route.giftRoute(giftService: GiftService) {

    get("/gift/{id}") {
        try {
            val id = call.requirePositiveId()
            val gift = giftService.findById(id)

            if (gift != null) {
                call.respond(HttpStatusCode.OK, gift)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    MessageResponse(
                        success = false,
                        message = "Подарок не найден"
                    )
                )
            }
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(
                    success = false,
                    message = e.message ?: "Некорректный запрос"
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse(
                    success = false,
                    message = "Внутренняя ошибка сервера"
                )
            )
        }
    }

    authenticate("auth-jwt") {

        post("/gift") {
            try {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw UnauthorizedException("Требуется авторизация")

                val userId = principal.payload
                    .getClaim("userId")
                    .asInt()
                    ?: throw UnauthorizedException("Некорректный токен")

                val gift = call.receive<Gift>()
                val createdGift = giftService.create(gift, userId)

                call.respond(HttpStatusCode.Created, createdGift)
            } catch (e: UnauthorizedException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Нет прав на создание подарка"
                    )
                )
            } catch (e: BadRequestException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Некорректные данные"
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Некорректные данные запроса"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MessageResponse(
                        success = false,
                        message = "Внутренняя ошибка сервера"
                    )
                )
            }
        }

        post("/gift/{id}/reserve") {
            try {
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
            } catch (e: UnauthorizedException) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Ошибка авторизации"
                    )
                )
            } catch (e: BadRequestException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Некорректный запрос"
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Конфликт при бронировании"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MessageResponse(
                        success = false,
                        message = "Внутренняя ошибка сервера"
                    )
                )
            }
        }

        post("/gift/{id}/unreserve") {
            try {
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
            } catch (e: UnauthorizedException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Нет прав на разрезервирование"
                    )
                )
            } catch (e: BadRequestException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Некорректный запрос"
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Конфликт при разрезервировании"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MessageResponse(
                        success = false,
                        message = "Внутренняя ошибка сервера"
                    )
                )
            }
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
                    call.respond(
                        HttpStatusCode.OK,
                        MessageResponse(
                            success = true,
                            message = "Подарок успешно удален"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        MessageResponse(
                            success = false,
                            message = "Подарок не найден"
                        )
                    )
                }
            } catch (e: UnauthorizedException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Нет прав на удаление подарка"
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse(
                        success = false,
                        message = e.message ?: "Некорректный ID подарка"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MessageResponse(
                        success = false,
                        message = "Внутренняя ошибка сервера"
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