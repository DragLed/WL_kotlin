package com.wishlistApp.service

import com.wishlistApp.model.Gift
import com.wishlistApp.repository.GiftRepository

class GiftService(
    private val giftRepository: GiftRepository
) {

    fun create(gift: Gift): Gift {
        return giftRepository.create(gift)
    }

    fun findById(id: Int): Gift? {
        return giftRepository.findById(id)
    }

    fun findAll(): List<Gift> {
        return giftRepository.findAll()
    }

    fun delete(id: Int): Boolean {
        return giftRepository.delete(id)
    }
}