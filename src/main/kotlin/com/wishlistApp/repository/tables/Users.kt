package com.wishlistApp.repository

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}