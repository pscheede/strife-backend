package com.scheede

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.github.cdimascio.dotenv.dotenv
import java.lang.Exception

object DB {
    private val client = run {
        val dotenv = dotenv()
        dotenv["MONGODB_CONNECTION_URI"]?.let { return@run MongoClient.create(it) }
        throw Exception("please specifiy MONGODB_CONNECTION_URI env variable")
    }

    private val database = client.getDatabase("strife");

    internal inline fun <reified T : Any> getCollection(name: String): MongoCollection<T> {
        return database.getCollection<T>(name)
    }
}