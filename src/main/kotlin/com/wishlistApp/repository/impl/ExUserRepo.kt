package com.wishlistApp.repository.impl

import com.wishlistApp.model.User
import com.wishlistApp.repository.UserRepository
import com.wishlistApp.repository.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ExUserRepo : UserRepository {
    
    override fun post(user: User): User {
        val id = Users.insert {
            it[username] = user.username
            it[password] = user.password
            it[createdAt] = user.createdAt
        }[Users.id]
        return user.copy(id = id)
    }

    override fun findById(id: Int): User? {
            return Users.select { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()

    }

    override fun findAll(): List<User> {
        return Users.selectAll().map { it.toUser() }
    }

    // override fun update(user: User): Boolean {
    //     return transaction {
    //         Users.update({ Users.id eq user.id }) {
    //             it[name] = user.name
    //             it[email] = user.email
    //         } > 0
    //     }
    // }


    override fun delete(id: Int): Boolean {
        return Users.deleteWhere { Users.id eq id } > 0
    }

    private fun ResultRow.toUser() =  User(
        id = this[Users.id],
        username = this[Users.username],
        password = this[Users.password],
        createdAt = this[Users.createdAt]
        )

}