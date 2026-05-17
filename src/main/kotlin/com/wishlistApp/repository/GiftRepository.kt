package com.wishlistApp.repository

import com.wishlistApp.model.Gift

interface GiftRepository {

    fun create(gift: Gift, UserId: Int): Gift
    fun findById(id: Int): Gift?
    fun delete(giftId: Int, UserId: Int): Boolean
    fun reserve(id: Int, userId: Int): Boolean
    fun unreserve(id: Int, userId: Int): Boolean

}