package com.wishlistApp.repository

import org.jetbrains.exposed.sql.Table
import com.wishlistApp.core.WishlistVisibility

object Wishlists : Table("wishlists") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", Users.id)
    val title = text("title")
    val description = text("description")

//    val createdAt = text("created_at", 255).nullable()

    val visibility = enumerationByName(
        "visibility",
        20,
        WishlistVisibility::class
    )

    override val primaryKey = PrimaryKey(id)
}