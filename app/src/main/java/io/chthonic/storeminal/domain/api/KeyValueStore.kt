package io.chthonic.storeminal.domain.api

interface KeyValueStore {
    suspend fun set(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun delete(key: String): String?
    suspend fun count(value: String): Int
    suspend fun beginTransaction()
    suspend fun commitTransaction()
    suspend fun rollbackTransaction()
}

class NoTransactionException : RuntimeException()