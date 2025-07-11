package com.scheede.auth

import com.mongodb.client.model.Filters
import com.scheede.DB
import kotlinx.coroutines.flow.firstOrNull

data class Key(val key: String, val user: String)

class KeyRepository {
    private val collection = DB.getCollection<Key>("keys")

    suspend fun getMatchingKey(key: String): Key? {
        return collection.find(Filters.eq(Key::key.name, key)).firstOrNull()
    }

    suspend fun getKeyForUser(user: String): Key? {
        return collection.find(Filters.eq(Key::user.name, user)).firstOrNull()
    }
}