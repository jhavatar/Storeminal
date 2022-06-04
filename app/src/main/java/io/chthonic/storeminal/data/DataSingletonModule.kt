package io.chthonic.storeminal.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.chthonic.storeminal.data.memory.MemoryStore
import io.chthonic.storeminal.domain.api.KeyValueStore

@Module
@InstallIn(SingletonComponent::class)
class DataSingletonModule {
    @Provides
    fun provideKeyValueStore(impl: MemoryStore): KeyValueStore =
        impl
}