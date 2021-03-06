package io.chthonic.storeminal.domain.api

interface KeyValueStore {

    /**
     *  Store the value for key.
     */
    fun set(key: String, value: String)

    /**
     * Return the current value for [key]. Returns null if the key does not have a value.
     */
    fun get(key: String): String?

    /**
     * Remove the value for [key] and returns it. Returns null if the key does not have a value.
     */
    fun delete(key: String): String?

    /**
     * Return the number of keys that have the given [value]. Returns 0 if no keys have the value.
     */
    fun count(value: String): Int

    /**
     * Start a new active transaction.
     */
    fun beginTransaction()

    /**
     * Complete the current transaction.
     * @throws [NoTransactionException] if there isn't an active transaction in progress.
     */
    fun commitTransaction()

    /**
     * Revert to a state prior to the current active transaction, i.e. before [beginTransaction].
     * @throws [NoTransactionException] if there isn't a transaction in progress.
     */
    fun rollbackTransaction()
}

/**
 * Command required an active transaction which did not exist.
 */
class NoTransactionException : RuntimeException()