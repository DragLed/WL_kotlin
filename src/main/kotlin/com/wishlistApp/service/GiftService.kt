package com.wishlistApp.service

import com.wishlistApp.exception.ForbiddenException
import com.wishlistApp.model.Gift
import com.wishlistApp.repository.GiftRepository
import io.ktor.server.plugins.NotFoundException

class GiftService(
    private val giftRepository: GiftRepository
) {

    fun create(gift: Gift, UserId: Int): Gift {
        return giftRepository.create(gift, UserId)
    }

    fun findById(id: Int): Gift? {
        return giftRepository.findById(id)
            ?: throw NotFoundException("Подарок с id=$id не найден")
    }

    fun delete(giftId: Int, UserId: Int): Boolean {
        val deleted = giftRepository.delete(giftId, UserId)

        when {
            deleted -> return true

            giftRepository.findById(giftId) == null ->
                throw NotFoundException("Подарок с id=$giftId не найден")

            else ->
                throw ForbiddenException("Вы не можете удалить чужой подарок")
        }
    }

    fun reserve(id: Int, userId: Int): Boolean {
        return giftRepository.reserve(id, userId)
    }
    fun unreserve(id: Int, userId: Int): Boolean {
        return giftRepository.unreserve(id, userId)
    }
}