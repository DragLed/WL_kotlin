package com.wishlistApp

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import com.wishlistApp.service.UserService
import com.wishlistApp.dto.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.configureRoutes(service: UserService) {

    post("/auth") {
        val request = call.receive<CreateUserRequest>()
        try {
            val user = transaction {
                service.addUser(
                    request.username,
                    request.password, 

                )
            }
            call.respond(HttpStatusCode.Created, user)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

}