package com.wishlistApp.repository.impl

import com.wishlistApp.model.Wishlist
import com.wishlistApp.repository.WishlistRepository
import com.wishlistApp.repository.Wishlists
import com.wishlistApp.service.WishlistAccessService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import com.wishlistApp.model.Gift
import com.wishlistApp.repository.tables.Gifts


class ExWishlistRepo : WishlistRepository {

    private val accessService = WishlistAccessService()

    override fun create(wishlist: Wishlist): Wishlist {
        val moscowTime = java.time.LocalDateTime
            .now(java.time.ZoneId.of("Europe/Moscow"))
            .toString()

        return transaction {
            val id = Wishlists.insert {
                it[userId] = wishlist.userId
                it[title] = wishlist.title
                it[description] = wishlist.description ?: ""
                it[visibility] = wishlist.visibility
                it[createdAt] = moscowTime
            }[Wishlists.id]

            wishlist.copy(
                id = id,
                createdAt = moscowTime
            )
        }
    }

    override fun findById(user_id: Int, wishlist_id: Int): Wishlist? {
        val wishlist = transaction {
            Wishlists
                .selectAll()
                .where { Wishlists.id eq wishlist_id }
                .map { it.toWishlist() }
                .singleOrNull()
        } ?: return null

        return if (accessService.canView(wishlist, user_id)) {
            wishlist
        } else {
            null
        }
    }

    override fun findAll(): List<Wishlist> {
        return transaction {
            Wishlists
                .selectAll()
                .map { it.toWishlist() }
        }
    }

    override fun update(id: Int, wishlist: Wishlist): Boolean {
        val existingWishlist = transaction {
            Wishlists
                .selectAll()
                .where { Wishlists.id eq id }
                .map { it.toWishlist() }
                .singleOrNull()
        } ?: return false

        if (!accessService.canEdit(existingWishlist, wishlist.userId)) {
            return false
        }

        return transaction {
            Wishlists.update({ Wishlists.id eq id }) {
                it[userId] = wishlist.userId
                it[title] = wishlist.title
                it[description] = wishlist.description ?: ""
                it[visibility] = wishlist.visibility
            } > 0
        }
    }

    override fun delete(id: Int): Boolean {
        return transaction {
            Wishlists.deleteWhere {
                Wishlists.id eq id
            } > 0
        }
    }

    fun delete(id: Int, currentUserId: Int): Boolean {
        val wishlist = transaction {
            Wishlists
                .selectAll()
                .where { Wishlists.id eq id }
                .map { it.toWishlist() }
                .singleOrNull()
        } ?: return false

        if (!accessService.canDelete(wishlist, currentUserId)) {
            return false
        }

        return transaction {
            Wishlists.deleteWhere {
                Wishlists.id eq id
            } > 0
        }
    }

    private fun ResultRow.toWishlist(): Wishlist {
        val wishlistId = this[Wishlists.id]
        val gifts = Gifts
            .selectAll()
            .where { Gifts.wishlistId eq wishlistId }
            .map { giftRow ->
                Gift(
                    id = giftRow[Gifts.id],
                    WishListId = giftRow[Gifts.wishlistId],
                    title = giftRow[Gifts.title],
                    description = giftRow[Gifts.description],
                    price = giftRow[Gifts.price],
                    is_reserved = giftRow[Gifts.isReserved],
                    reserved_by = giftRow[Gifts.reservedBy] ?: 0,
                    createdAt = giftRow[Gifts.createdAt].toString()
                )
            }

        return Wishlist(
            id = wishlistId,
            userId = this[Wishlists.userId],
            title = this[Wishlists.title],
            description = this[Wishlists.description],
            visibility = this[Wishlists.visibility],
            createdAt = this[Wishlists.createdAt],
            gifts = gifts
        )
    }
}
