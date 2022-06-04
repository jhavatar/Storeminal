package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString

interface ExecuteCommandLineInputUseCase {

    /**
     * Execute a single command line input and return a possible result.
     * @throws UnknownCommandException if input's command is not recognized.
     * @throws KeyNotSetException if command references requires a key which has not been set.
     * @throws NoTransactionException if command required an active transaction which did not exist.
     */
    suspend fun execute(input: InputString): String?
}