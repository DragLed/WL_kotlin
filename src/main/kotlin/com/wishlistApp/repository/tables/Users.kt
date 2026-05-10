package com.wishlistApp.repository

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val password = varchar("password", 255)
    val createdAt = varchar("created_at", 30)
    override val primaryKey = PrimaryKey(id)
}