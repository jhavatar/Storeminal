package io.chthonic.storeminal.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.chthonic.storeminal.data.memory.ConcurrentMemoryStore
import io.chthonic.storeminal.domain.api.KeyValueStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSingletonModule {
    @Provides
    @Singleton
    fun provideKeyValueStore(): KeyValueStore =
        ConcurrentMemoryStore()
}