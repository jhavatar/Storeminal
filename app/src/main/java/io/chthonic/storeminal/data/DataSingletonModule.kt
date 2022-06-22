package io.chthonic.storeminal.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.chthonic.storeminal.BuildConfig
import io.chthonic.storeminal.data.store.concurrent.ConcurrentFlowSequentializedStore
import io.chthonic.storeminal.data.store.concurrent.ConcurrentMutexedStore
import io.chthonic.storeminal.data.store.concurrent.StoreFlowSequentializer
import io.chthonic.storeminal.data.store.memory.MemoryStore
import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import io.chthonic.storeminal.domain.api.KeyValueStore
import javax.inject.Singleton

private const val MUTEXED = "Mutexed"

@Module
@InstallIn(SingletonComponent::class)
class DataSingletonModule {

    @Provides
    @Singleton
    internal fun provideKeyValueStore(): KeyValueStore = MemoryStore(null)

    @Provides
    @Singleton
    internal fun provideConcurrentKeyValueStore(
        coreStore: KeyValueStore
    ): ConcurrentKeyValueStore = if (BuildConfig.FLAVOR.equals(MUTEXED, ignoreCase = true)) {
        ConcurrentMutexedStore(coreStore)
    } else {
        ConcurrentFlowSequentializedStore(StoreFlowSequentializer(coreStore))
    }
}