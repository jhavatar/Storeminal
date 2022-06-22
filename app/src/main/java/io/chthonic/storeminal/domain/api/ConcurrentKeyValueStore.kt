package io.chthonic.storeminal.domain.api

interface ConcurrentKeyValueStore {

    /**
     *  Store the value for key.
     */
    suspend fun set(key: String, value: String)

    /**
     * Return the current value for [key]. Returns null if the key does not have a value.
     */
    suspend fun get(key: String): String?

    /**
     * Remove the value for [key] and returns it. Returns null if the key does not have a value.
     */
    suspend fun delete(key: String): String?

    /**
     * Return the number of keys that have the given [value]. Returns 0 if no keys have the value.
     */
    suspend fun count(value: String): Int

    /**
     * Start a new active transaction.
     */
    suspend fun beginTransaction()

    /**
     * Complete the current transaction.
     * @throws [NoTransactionException] if there isn't an active transaction in progress.
     */
    suspend fun commitTransaction()

    /**
     * Revert to a state prior to the current active transaction, i.e. before [beginTransaction].
     * @throws [NoTransactionException] if there isn't a transaction in progress.
     */
    suspend fun rollbackTransaction()
}