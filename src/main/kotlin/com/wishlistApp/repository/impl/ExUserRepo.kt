package com.wishlistApp.repository.impl

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository
import com.wishlistApp.repository.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and


class ExUserRepo : UserRepository {


    override fun create(user: User): User {
        return transaction {
            val moscowTime = java.time.LocalDateTime
                .now(java.time.ZoneId.of("Europe/Moscow"))
                .toString()

            val id = Users.insert {
                it[username] = user.username
                it[password] = user.password
                it[createdAt] = moscowTime
            }[Users.id]

            user.copy(
                id = id,
                createdAt = moscowTime
            )
        }
    }


    override fun updatename(id: Int, username: String): Boolean {
        return transaction {
            Users.update({ Users.id eq id }) {
                it[Users.username] = username
            } > 0
        }
    }

    override fun updatepassword(id: Int, password: String): Boolean {
        return transaction {
            Users.update({ Users.id eq id }) {
                it[Users.password] = password
            } > 0
        }
    }


    override fun delete(id: Int): Boolean {
        return transaction {   Users.deleteWhere { Users.id eq id } > 0
        }
    }

    override fun findByUsername(username: String): User? {
        return transaction {
            Users
                .selectAll()
                .where { Users.username eq username }
                .map { it.toUser() }
                .singleOrNull()
        }
    }

    override fun findById(id: Int): User? {
        return transaction {
            Users
                .selectAll()
                .where { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }
    }

    override fun findAll(): List<User> {
        return transaction {
            Users
                .selectAll()
                .map { it.toUser() }
        }
    }

    private fun ResultRow.toUser() = User(
        id = this[Users.id],
        username = this[Users.username],
        password = this[Users.password],
        createdAt = this[Users.createdAt]
    )
}