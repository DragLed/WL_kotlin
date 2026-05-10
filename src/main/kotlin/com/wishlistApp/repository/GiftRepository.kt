package com.wishlistApp.repository

import com.wishlistApp.model.Gift

interface GiftRepository {

    fun create(gift: Gift): Gift
    fun findById(id: Int): Gift?
    fun findAll(): List<Gift>
    fun delete(id: Int): Boolean

}