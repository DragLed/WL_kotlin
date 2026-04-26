package com.wishlistApp

import com.wishlistApp.repository.impl.ExUserRepo
import com.wishlistApp.service.UserService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import java.io.PrintStream


fun main() {
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    System.setIn(System.`in`.buffered())

    // Подключение к PostgreSQL
    // TODO: укажите свой URL, user и password!
    Database.connect(
        url = "jdbc:postgresql://",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "0000"
    )

    // Создание репозиториев и сервиса
    val userRepo   = ExUserRepo()
    val service    = UserService(userRepo)

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()  // поддержка JSON
        }


        routing {
            // OpenAPI JSON
            openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

            // Swagger UI
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

            configureRoutes(service)  // маршруты
        }
    }.start(wait = true)
}
