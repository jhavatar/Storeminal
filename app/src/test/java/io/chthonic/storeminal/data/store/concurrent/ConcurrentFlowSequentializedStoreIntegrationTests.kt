package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.data.store.memory.MemoryStore
import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class ConcurrentFlowSequentializedStoreIntegrationTests :
    ConcurrentStoreIntegrationTestsTemplate() {

    override fun buildTested(coreStore: MemoryStore): ConcurrentKeyValueStore =
        ConcurrentFlowSequentializedStore(
            StoreFlowSequentializer(
                store = coreStore,
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            )
        )
}