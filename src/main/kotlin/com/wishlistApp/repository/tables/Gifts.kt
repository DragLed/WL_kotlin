package com.wishlistApp.repository.tables

import com.wishlistApp.repository.Wishlists
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Gifts : Table("gifts") {
    val id = integer("id").autoIncrement()
    val wishlistId = reference("wishlist_id", Wishlists.id)
    val title = varchar("title", 255)
    val description = text("description")
    val price = float("price")
    val photo = text("photo").nullable()
    val isReserved = bool("is_reserved").default(false)
    val reservedBy = integer("reserved_by").nullable()
    val createdAt = varchar("created_at", 30)

    override val primaryKey = PrimaryKey(id)
}