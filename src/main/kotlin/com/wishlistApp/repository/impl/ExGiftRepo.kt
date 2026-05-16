package com.wishlistApp.repository.impl

import com.wishlistApp.core.WishlistVisibility
import com.wishlistApp.exception.ForbiddenException
import com.wishlistApp.model.Gift
import com.wishlistApp.repository.GiftRepository
import com.wishlistApp.repository.Users
import com.wishlistApp.repository.tables.Gifts
import com.wishlistApp.repository.Wishlists
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ExGiftRepo : GiftRepository {

    override fun create(gift: Gift): Gift {
        val moscowTime = java.time.LocalDateTime
            .now(java.time.ZoneId.of("Europe/Moscow"))
            .toString()

        return transaction {
            val id = Gifts.insert {
                it[wishlistId] = gift.WishListId
                it[title] = gift.title
                it[description] = gift.description
                it[price] = gift.price
                it[photo] = null
                it[reservedBy] = null
                it[createdAt] = moscowTime
            }[Gifts.id]

            gift.copy(
                id = id,
                createdAt = moscowTime
            )
        }
    }

    override fun findById(id: Int): Gift? {
        return transaction {
            Gifts
                .innerJoin(Wishlists)
                .selectAll()
                .where {
                    (Gifts.id eq id) and (
                            (Wishlists.visibility eq WishlistVisibility.public) or
                                    (Wishlists.visibility eq WishlistVisibility.link_only)
                            )
                }
                .map { it.toGift() }
                .singleOrNull()
        }
    }

    override fun reserve(id: Int, userId: Int): Boolean {
        return transaction {
            val gift = Gifts
                .selectAll()
                .where { Gifts.id eq id }
                .singleOrNull()
                ?: throw NotFoundException("Подарок с id=$id не найден")

            if (gift[Gifts.isReserved]) {
                throw BadRequestException(
                    "Этот подарок уже зарезервирован другим пользователем"
                )
            }

            Gifts.update({ Gifts.id eq id }) {
                it[Gifts.isReserved] = true
                it[Gifts.reservedBy] = userId
            } > 0
        }
    }

    override fun unreserve(id: Int, userId: Int): Boolean {
        return transaction {
            val gift = Gifts
                .selectAll()
                .where { Gifts.id eq id }
                .singleOrNull()
                ?: throw NotFoundException("Подарок с id=$id не найден")

            if (!gift[Gifts.isReserved]) {
                throw BadRequestException("Подарок не зарезервирован")
            }

            if (gift[Gifts.isReserved]) {
                throw BadRequestException(
                    "Вы не можете снять чужую бронь"
                )
            }

            Gifts.update({ Gifts.id eq id }) {
                it[Gifts.isReserved] = false
                it[Gifts.reservedBy] = null
            } > 0
        }
    }

    override fun delete(id: Int, userId: Int): Boolean {
        return transaction {

            // Получаем подарок
            val gift = Gifts
                .selectAll()
                .where { Gifts.id eq id }
                .singleOrNull()
                ?: return@transaction false

            // Получаем вишлист подарка
            val wishlist = Wishlists
                .selectAll()
                .where { Wishlists.id eq gift[Gifts.wishlistId] }
                .singleOrNull()
                ?: return@transaction false

            // Проверяем, что текущий пользователь является владельцем вишлиста
            if (wishlist[Wishlists.userId] != userId) {
                return@transaction false
            }

            // Удаляем подарок
            Gifts.deleteWhere { Gifts.id eq id } > 0
        }
    }

    private fun ResultRow.toGift() = Gift(
        id = this[Gifts.id],
        WishListId = this[Gifts.wishlistId],
        title = this[Gifts.title],
        description = this[Gifts.description],
        price = this[Gifts.price],
        is_reserved = this[Gifts.isReserved],
        reserved_by = this[Gifts.reservedBy] ?: 0,
        createdAt = this[Gifts.createdAt].toString()
    )
}