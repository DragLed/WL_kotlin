package com.wishlistApp.repository.impl

import com.wishlistApp.model.Gift
import com.wishlistApp.repository.GiftRepository
import com.wishlistApp.repository.tables.Gifts
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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

            gift.copy(id = id,
                createdAt = moscowTime)
        }
    }

    override fun findById(id: Int): Gift? {
        return transaction {
            Gifts
                .selectAll()
                .where { Gifts.id eq id }
                .map { it.toGift() }
                .singleOrNull()
        }
    }

    override fun findAll(): List<Gift> {
        return transaction {
            Gifts.selectAll().map { it.toGift() }
        }
    }

    override fun delete(id: Int): Boolean {
        return transaction {
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