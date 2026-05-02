package com.wishlistApp.repository.impl

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository
import com.wishlistApp.repository.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ExUserRepo : UserRepository {

    override fun create(user: User): User {
        return transaction {
            val id = Users.insert {
                it[username] = user.username
                it[password] = user.password
            }[Users.id]

            user.copy(id = id)
        }
    }


    override fun findById(id: Int): User? {
            return transaction {   Users.select { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()
            }
    }

    override fun findAll(): List<User> {
        return transaction { Users.selectAll().map { it.toUser() } }

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

    private fun ResultRow.toUser() =  User(
        id = this[Users.id],
        username = this[Users.username],
        password = this[Users.password]
        )

}