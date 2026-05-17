package com.wishlistApp.routing

import com.wishlistApp.dto.CreateWishlistRequest
import com.wishlistApp.service.WishlistService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.slf4j.LoggerFactory
import kotlin.text.toIntOrNull

private val logger = LoggerFactory.getLogger("WishlistRoute")

fun Route.wishlistRoute(wishlistService: WishlistService) {

    authenticate("auth-jwt") {

        post("/wishlist") {
            try {
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@post
                }

                val userId = try {
                    principal.payload.getClaim("userId").asInt()
                } catch (e: Exception) {
                    logger.error("Failed to extract userId from token", e)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid token format")
                    )
                    return@post
                }

                if (userId <= 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid user ID in token")
                    )
                    return@post
                }

                val request = try {
                    call.receive<CreateWishlistRequest>()
                } catch (e: Exception) {
                    logger.error("Failed to receive request body", e)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request body format")
                    )
                    return@post
                }

                if (request.title.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Wishlist title cannot be empty")
                    )
                    return@post
                }

                val wishlist = try {
                    wishlistService.create(
                        userId = userId,
                        title = request.title.trim(),
                        description = request.description.trim(),
                        visibility = request.visibility
                    )
                } catch (e: IllegalArgumentException) {
                    logger.warn("Validation error while creating wishlist: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Invalid wishlist data"))
                    )
                    return@post
                } catch (e: Exception) {
                    logger.error("Unexpected error while creating wishlist", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to create wishlist")
                    )
                    return@post
                }

                call.respond(HttpStatusCode.Created, wishlist)
            } catch (e: Exception) {
                logger.error("Unhandled exception in POST /wishlist", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal server error")
                )
            }
        }

        get("/wishlist/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@get
                }

                val userId = try {
                    principal.payload.getClaim("userId").asInt()
                } catch (e: Exception) {
                    logger.error("Failed to extract userId from token", e)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid token format")
                    )
                    return@get
                }

                val wishlistId = try {
                    call.requirePositiveId()
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                    return@get
                }

                val wishlist = try {
                    wishlistService.getById(
                        currentUserId = userId,
                        wishlistId = wishlistId
                    )
                } catch (e: IllegalArgumentException) {
                    logger.warn("Access denied for user $userId to wishlist $wishlistId: ${e.message}")
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Wishlist not found or access denied")
                    )
                    return@get
                } catch (e: Exception) {
                    logger.error("Unexpected error while fetching wishlist", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to fetch wishlist")
                    )
                    return@get
                }

                if (wishlist != null) {
                    call.respond(HttpStatusCode.OK, wishlist)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Wishlist not found or access denied")
                    )
                }
            } catch (e: Exception) {
                logger.error("Unhandled exception in GET /wishlist/{id}", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal server error")
                )
            }
        }

        delete("/wishlist/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@delete
                }

                val userId = try {
                    principal.payload.getClaim("userId").asInt()
                } catch (e: Exception) {
                    logger.error("Failed to extract userId from token", e)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid token format")
                    )
                    return@delete
                }

                val wishlistId = try {
                    call.requirePositiveId()
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                    return@delete
                }

                val deleted = try {
                    wishlistService.delete(
                        wishlistId = wishlistId,
                        currentUserId = userId
                    )
                } catch (e: IllegalArgumentException) {
                    logger.warn("Access denied for user $userId to delete wishlist $wishlistId: ${e.message}")
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Access denied or wishlist not found")
                    )
                    return@delete
                } catch (e: Exception) {
                    logger.error("Unexpected error while deleting wishlist", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to delete wishlist")
                    )
                    return@delete
                }

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("deleted" to true)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Access denied or wishlist not found")
                    )
                }
            } catch (e: Exception) {
                logger.error("Unhandled exception in DELETE /wishlist/{id}", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal server error")
                )
            }
        }
    }

    get("/wishlists") {
        try {
            val wishlists = try {
                wishlistService.getAll()
            } catch (e: Exception) {
                logger.error("Failed to fetch all wishlists", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to fetch wishlists")
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, wishlists)
        } catch (e: Exception) {
            logger.error("Unhandled exception in GET /wishlists", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}

private fun ApplicationCall.requirePositiveId(): Int {
    val idString = parameters["id"]

    if (idString == null) {
        throw IllegalArgumentException("Missing 'id' parameter")
    }

    val id = idString.toIntOrNull()

    if (id == null) {
        throw IllegalArgumentException("Invalid id format. Expected integer, got: $idString")
    }

    if (id <= 0) {
        throw IllegalArgumentException("Invalid id. Must be a positive integer, got: $id")
    }

    return id
}