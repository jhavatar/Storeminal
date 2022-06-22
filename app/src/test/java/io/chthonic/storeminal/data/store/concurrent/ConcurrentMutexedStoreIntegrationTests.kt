package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.data.store.memory.MemoryStore
import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore

internal class ConcurrentMutexedStoreIntegrationTests : ConcurrentStoreIntegrationTestsTemplate() {

    override fun buildTested(coreStore: MemoryStore): ConcurrentKeyValueStore =
        ConcurrentMutexedStore(coreStore)
}