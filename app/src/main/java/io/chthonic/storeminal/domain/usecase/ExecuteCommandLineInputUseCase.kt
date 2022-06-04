package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString

interface ExecuteCommandLineInputUseCase {

    /**
     * Execute a single command line input and return a possible result.
     * @throws UnknownCommandException if input's command is not recognized.
     * @throws KeyNotSetException if command references a value at a key that has not been set.
     * @throws NoTransactionException if command requires an active transaction which does not exist.
     */
    suspend fun execute(input: InputString): String?
}