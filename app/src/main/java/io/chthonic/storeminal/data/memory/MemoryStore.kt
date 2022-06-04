package io.chthonic.storeminal.data.memory

import io.chthonic.storeminal.domain.api.KeyValueStore
import io.chthonic.storeminal.domain.api.NoTransactionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory implementation of [KeyValueStore]
 * @param initialState The initial state of the store, default is null. Can be used for testing and to restore the store from persistence.
 */
internal class MemoryStore(initialState: ArrayDeque<ConcurrentHashMap<String, String>>? = null) :
    KeyValueStore {

    private val transMutex = Mutex()
    private val transStack =
        initialState ?: ArrayDeque<ConcurrentHashMap<String, String>>(1).apply {
            this.addLast(ConcurrentHashMap<String, String>())
        }

    override suspend fun set(key: String, value: String) {
        getActiveTrans()[key] = value
    }

    override suspend fun get(key: String): String? = getActiveTrans()[key]

    override suspend fun delete(key: String): String? = getActiveTrans().remove(key)

    override suspend fun count(value: String): Int = withContext(Dispatchers.Default) {
        getActiveTrans().values.count {
            it == value
        }
    }

    override suspend fun beginTransaction() {
        transMutex.withLock {
            transStack.addLast(ConcurrentHashMap<String, String>())
        }
    }

    override suspend fun commitTransaction() {
        withContext(Dispatchers.Default) {
            transMutex.withLock {
                val oldTrans = removeActiveTransLockFree() ?: throw NoTransactionException()
                val trans = getActiveTransLockFree()
                oldTrans.forEach {
                    trans[it.key] = it.value
                }
            }
        }
    }

    override suspend fun rollbackTransaction() {
        removeActiveTrans() ?: throw NoTransactionException()
    }

    private fun getActiveTransLockFree(): ConcurrentHashMap<String, String> =
        transStack.last()

    private suspend fun getActiveTrans(): ConcurrentHashMap<String, String> =
        transMutex.withLock {
            getActiveTransLockFree()
        }

    private fun removeActiveTransLockFree(): ConcurrentHashMap<String, String>? =
        if (transStack.size > 1) {
            transStack.removeLast()
        } else null

    private suspend fun removeActiveTrans(): ConcurrentHashMap<String, String>? =
        transMutex.withLock {
            removeActiveTransLockFree()
        }
}