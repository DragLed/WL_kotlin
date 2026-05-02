package com.wishlistApp.repository.impl

import com.wishlistApp.model.Wishlist
import com.wishlistApp.repository.WishlistRepository
import com.wishlistApp.repository.Wishlists
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ExWishlistRepo : WishlistRepository {

    override fun create(wishlist: Wishlist): Wishlist {
        return transaction {
            val id = Wishlists.insert {
                it[userId] = wishlist.userId
                it[title] = wishlist.title
                it[description] = wishlist.description
//                it[createdAt] = wishlist.createdAt
                it[visibility] = wishlist.visibility
            }[Wishlists.id]

            wishlist.copy(id = id)
        }
    }


//     override fun findById(id: Int): Wishlist? {
//             return transaction {   Wishlists.select { Wishlists.id eq id }
//                 .map { it.toWishlist() }
//                 .singleOrNull()
//             }
//     }

     override fun findAll(): List<Wishlist> {
         return transaction { Wishlists.selectAll().map { it.toWishlist() } }
     }

//     override fun update(id: Int, wishlist: Wishlist): Boolean {
//         return transaction {
//             Wishlists.update({ Wishlists.id eq id }) {
//                 it[Wishlists.userId] = wishlist.userId
//                 it[Wishlists.title] = wishlist.title
//                 it[Wishlists.description] = wishlist.description
//                 it[Wishlists.createdAt] = wishlist.createdAt
//                 it[Wishlists.visibility] = wishlist.visibility
//             } > 0
//         }
//     }

//     override fun delete(id: Int): Boolean {
//         return transaction {   Wishlists.deleteWhere { Wishlists.id eq id } > 0
//         }
//     }

//     private fun ResultRow.toWishlist() =  Wishlist(
//         id = this[Wishlists.id],
//         userId = this[Wishlists.userId],
//         title = this[Wishlists.title],
//         description = this[Wishlists.description],
//         createdAt = this[Wishlists.createdAt],
//         visibility = this[Wishlists.visibility]
//         )

//                 it[Users.password] = password
//             } > 0
//         }
//     }


//     override fun delete(id: Int): Boolean {
//         return transaction {   Wishlists.deleteWhere { Wishlists.id eq id } > 0
//         }
//     }

    private fun ResultRow.toWishlist() =  Wishlist(
        id = this[Wishlists.id],
        userId = this[Wishlists.userId],
        title = this[Wishlists.title],
        description = this[Wishlists.description],
//        createdAt = this[Wishlists.createdAt],
        visibility = this[Wishlists.visibility]
        )

}