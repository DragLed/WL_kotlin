package com.wishlistApp

import com.wishlistApp.core.JwtConfig
import com.wishlistApp.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import com.wishlistApp.repository.Users
import com.wishlistApp.repository.WishlistRepository
import com.wishlistApp.repository.Wishlists
import com.wishlistApp.repository.impl.ExUserRepo
import com.wishlistApp.repository.impl.ExWishlistRepo
import com.wishlistApp.service.UserService
import com.wishlistApp.service.WishlistService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import org.jetbrains.exposed.sql.Database
import java.io.PrintStream
import io.ktor.server.http.content.*


fun main() {
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    System.setIn(System.`in`.buffered())

    // 🔥 НОРМАЛЬНОЕ подключение к PostgreSQL
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/wishlist_db", // измени под свою БД
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "0000"
    )

    transaction {
        SchemaUtils.create(Users, Wishlists)
    }

    val userRepo = ExUserRepo()
    val wishlistRepo = ExWishlistRepo()
    val UserService = UserService(userRepo)
    val WishlistService = WishlistService(wishlistRepo)

    embeddedServer(Netty, port = 8080) {


        install(io.ktor.server.auth.Authentication) {

            jwt("auth-jwt") {
                verifier(JwtConfig.verifier())

                validate { credential ->
                    val userId = credential.payload.getClaim("userId").asInt()
                    if (userId != null) {
                        io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }


        install(ContentNegotiation) { json() }


            routing {
                swaggerUI(path = "swagger", swaggerFile = "openapi.json")
                openAPI(path = "openapi", swaggerFile = "openapi.json")

            configureRoutes(UserService, WishlistService )
            }
                }.start(wait = true)
}