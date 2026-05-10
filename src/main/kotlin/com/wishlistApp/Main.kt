package com.wishlistApp

import com.wishlistApp.core.JwtConfig
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
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.plugins.statuspages.StatusPages
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    System.setIn(System.`in`.buffered())

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/wishlist_db",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "0000"
    )

    transaction {
        SchemaUtils.create(Users, Wishlists, Gifts)
    }

    val userRepo = ExUserRepo()
    val wishlistRepo = ExWishlistRepo()
    val giftRepo = ExGiftRepo()

    val userService = UserService(userRepo)
    val wishlistService = WishlistService(wishlistRepo)
    val giftService = GiftService(giftRepo)

    embeddedServer(Netty, port = 8080) {

        install(CallLogging) {
            level = Level.INFO
        }

        install(StatusPages) {
            exception<Throwable> { call, cause ->
                cause.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "error" to (cause.message ?: "Internal Server Error")
                    )
                )
            }
        }

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

        install(ContentNegotiation) {
            json()
        }

        routing {
            swaggerUI(
                path = "swagger",
                swaggerFile = "openapi.json"
            )

            openAPI(
                path = "openapi",
                swaggerFile = "openapi.json"
            )

            userRoute(userService)
            wishlistRoute(wishlistService)
            giftRoute(giftService)
        }

    }.start(wait = true)
}