package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import io.chthonic.storeminal.domain.api.KeyValueStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Implementation of [ConcurrentKeyValueStore] with thread safety enforced using a mutex (technically a [Semaphore] with one permit).
 */
internal class ConcurrentMutexedStore(val store: KeyValueStore) :
    ConcurrentKeyValueStore {

    // Use semaphore with one permit as mutex since kotlin semaphore is fair and maintains a FIFO order of acquirers while kotlin mutex does not
    private val transMutex = Semaphore(permits = 1)

    override suspend fun get(key: String): String? =
        transMutex.withPermit { store.get(key) }

    override suspend fun set(key: String, value: String) {
        transMutex.withPermit {
            store.set(key, value)
        }
    }

    override suspend fun delete(key: String): String? = transMutex.withPermit {
        store.delete(key)
    }

    override suspend fun count(value: String): Int = transMutex.withPermit {
        store.count(value)
    }

    override suspend fun beginTransaction() {
        transMutex.withPermit {
            store.beginTransaction()
        }
    }

    override suspend fun commitTransaction() {
        transMutex.withPermit {
            coroutineScope {
                // launch on background thread to not potentially block caller's thread since commit might take a while since the duration depends on the number of items in the transaction.
                launch(Dispatchers.Default) {
                    store.commitTransaction()
                }.join()
            }
        }
    }

    override suspend fun rollbackTransaction() {
        transMutex.withPermit {
            store.rollbackTransaction()
        }
    }
}