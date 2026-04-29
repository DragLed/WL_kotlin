package com.wishlistApp

import com.wishlistApp.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import com.wishlistApp.repository.Users
import com.wishlistApp.repository.impl.ExUserRepo
import com.wishlistApp.service.UserService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        SchemaUtils.create(Users)
    }

    val userRepo = ExUserRepo()
    val service = UserService(userRepo)

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {

            get("/openapi.json") {
                call.respondText(
                    this::class.java.classLoader
                        .getResource("openapi.json")!!
                        .readText(),
                    io.ktor.http.ContentType.Application.Json
                )
            }

            static("/swagger") {
                resources("swagger")
            }

            configureRoutes(service)
        }
    }.start(wait = true)
}