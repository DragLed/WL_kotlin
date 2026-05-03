package com.wishlistApp

import com.wishlistApp.core.JwtConfig
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import com.wishlistApp.repository.Users
import com.wishlistApp.repository.Wishlists
import com.wishlistApp.repository.impl.ExUserRepo
import com.wishlistApp.repository.impl.ExWishlistRepo
import com.wishlistApp.routing.userRoute
import com.wishlistApp.routing.wishlistRoute
import com.wishlistApp.service.UserService
import com.wishlistApp.service.WishlistService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import org.jetbrains.exposed.sql.Database
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

                userRoute(UserService)
                wishlistRoute(WishlistService)


            }
                }.start(wait = true)
}