package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.data.store.concurrent.StoreFlowSequentializer.Operation
import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore

/**
 * Implementation of [ConcurrentKeyValueStore] with thread safety enforced using [StoreFlowSequentializer].
 */
internal class ConcurrentFlowSequentializedStore constructor(private val storeFlowSequentializer: StoreFlowSequentializer) :
    ConcurrentKeyValueStore {

    override suspend fun set(key: String, value: String) {
        storeFlowSequentializer.emit(Operation.Set(key, value))
    }

    override suspend fun get(key: String): String? =
        storeFlowSequentializer.emit(Operation.Get(key)).value

    override suspend fun delete(key: String): String? =
        storeFlowSequentializer.emit(Operation.Delete(key)).value

    override suspend fun count(value: String): Int =
        storeFlowSequentializer.emit(Operation.Count(value)).value

    override suspend fun beginTransaction() {
        storeFlowSequentializer.emit(Operation.Begin)
    }

    override suspend fun commitTransaction() {
        storeFlowSequentializer.emit(Operation.Commit)
    }

    override suspend fun rollbackTransaction() {
        storeFlowSequentializer.emit(Operation.Rollback)
    }
}