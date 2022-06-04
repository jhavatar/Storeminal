package io.chthonic.storeminal.domain

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.chthonic.storeminal.domain.usecase.ExecuteCommandLineInputUseCase
import io.chthonic.storeminal.domain.usecase.ExecuteCommandLineInputUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
class DomainSingletonModule {

    @Provides
    internal fun provideExecuteCommandLineInputUseCase(
        impl: ExecuteCommandLineInputUseCaseImpl
    ): ExecuteCommandLineInputUseCase =
        impl
}