package com.wishlistApp

import com.wishlistApp.core.JwtConfig
import com.wishlistApp.dto.ErrorResponse
import com.wishlistApp.exception.ForbiddenException
import com.wishlistApp.exception.UnauthorizedException
import com.wishlistApp.repository.Users
import com.wishlistApp.repository.Wishlists
import com.wishlistApp.repository.impl.ExGiftRepo
import com.wishlistApp.repository.impl.ExUserRepo
import com.wishlistApp.repository.impl.ExWishlistRepo
import com.wishlistApp.repository.tables.Gifts
import com.wishlistApp.routing.giftRoute
import com.wishlistApp.routing.userRoute
import com.wishlistApp.routing.wishlistRoute
import com.wishlistApp.service.GiftService
import com.wishlistApp.service.UserService
import com.wishlistApp.service.WishlistService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level
import java.io.PrintStream

fun main() {
    // UTF-8 для корректного вывода кириллицы
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    System.setIn(System.`in`.buffered())

    // Подключение к PostgreSQL
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/wishlist_db",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "0000"
    )

    // Создание таблиц
    transaction {
        SchemaUtils.create(Users, Wishlists, Gifts)
    }

    // Репозитории
    val userRepo = ExUserRepo()
    val wishlistRepo = ExWishlistRepo()
    val giftRepo = ExGiftRepo()

    // Сервисы
    val userService = UserService(userRepo)
    val wishlistService = WishlistService(wishlistRepo)
    val giftService = GiftService(giftRepo)

    // Запуск сервера
    embeddedServer(Netty, port = 8080) {

        // Логирование
        install(CallLogging) {
            level = Level.INFO
        }

        // JSON сериализация
        install(ContentNegotiation) {
            json()
        }

        // JWT Authentication
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(JwtConfig.verifier())

                validate { credential ->
                    val userId = credential.payload
                        .getClaim("userId")
                        .asInt()

                    if (userId != null) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }

        // Глобальная обработка ошибок
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
                        message = cause.message ?: "Некорректный запрос",
                        code = 400
                    ))
            }

            // 403 Forbidden
            exception<ForbiddenException> { call, cause ->
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        message = cause.message ?: "Некорректный запрос",
                        code = 400
                    )
                )
            }

            // 404 Not Found
            exception<NotFoundException> { call, cause ->
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        message = cause.message ?: "Некорректный запрос",
                        code = 400
                    )
                )
            }

            // Ошибки базы данных
            exception<ExposedSQLException> { call, cause ->
                cause.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        message = cause.message ?: "Некорректный запрос",
                        code = 400
                    )
                )
            }

            // Любые остальные ошибки
            exception<Throwable> { call, cause ->
                cause.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        message = cause.message ?: "Некорректный запрос",
                        code = 400
                    )
                )
            }
        }

        // Роуты
        routing {

            // Swagger UI
            swaggerUI(
                path = "docs",
                swaggerFile = "openapi.json"
            )

            // OpenAPI
            openAPI(
                path = "openapi",
                swaggerFile = "openapi.json"
            )

            // API
            userRoute(userService)
            wishlistRoute(wishlistService)
            giftRoute(giftService)
        }

    }.start(wait = true)
}