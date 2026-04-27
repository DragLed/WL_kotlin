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

    post("/users") {
        val request = call.receive<CreateUserRequest>()
        try {
            val user = service.create(
                request.username,
                request.password
                )

            call.respond(HttpStatusCode.Created, user)

        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/users") {
        try {
            val users = service.getAll()
            call.respond(HttpStatusCode.OK, users)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/users/{id}") {
        try {
            val id = call.parameters["id"]?.toInt()
            require(id != null && id > 0) { "Некорректный id" }

            val user = service.getById(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            }
            else {
                call.respond(HttpStatusCode.NotFound)
            }


        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    delete("/users/{id}") {
        try {
            val id = call.parameters["id"]?.toInt()
            require(id != null && id > 0) { "Некорректный id" }

            val message = service.delete(id)
            if (message != null) {
                call.respond(HttpStatusCode.OK, message)
            }
            else {
                call.respond(HttpStatusCode.NotFound)
            }


        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }



}