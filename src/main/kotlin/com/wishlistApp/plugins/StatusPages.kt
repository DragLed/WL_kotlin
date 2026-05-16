package com.wishlistApp.plugins

import com.wishlistApp.dto.ErrorResponse
import com.wishlistApp.exception.ForbiddenException
import com.wishlistApp.exception.UnauthorizedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {

        // 400 Bad Request
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = cause.message ?: "Некорректный запрос",
                    code = 400
                )
            )
        }

        // 401 Unauthorized
        exception<UnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    message = cause.message ?: "Требуется авторизация",
                    code = 401
                )
            )
        }

        // 403 Forbidden
        exception<ForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    message = cause.message ?: "Доступ запрещён",
                    code = 403
                )
            )
        }

        // 404 Not Found
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    message = cause.message ?: "Ресурс не найден",
                    code = 404
                )
            )
        }

        // Ошибки базы данных
        exception<ExposedSQLException> { call, cause ->
            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Ошибка базы данных",
                    code = 500
                )
            )
        }

        // Любые остальные ошибки
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Внутренняя ошибка сервера",
                    code = 500
                )
            )
        }
    }
}