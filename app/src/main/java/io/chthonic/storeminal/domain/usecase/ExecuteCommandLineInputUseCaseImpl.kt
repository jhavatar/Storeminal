package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.model.Command
import io.chthonic.storeminal.domain.model.InputString
import javax.inject.Inject

internal class ExecuteCommandLineInputUseCaseImpl @Inject constructor(
    private val parseCommandUseCase: ParseCommandUseCase,
    private val store: ConcurrentKeyValueStore
) : ExecuteCommandLineInputUseCase {

    override suspend fun execute(input: InputString): String? {
        val command = parseCommandUseCase.execute(input)
        return executeCommand(command)
    }

    private suspend fun executeCommand(command: Command): String? {
        when (command) {
            is Command.Get -> return store.get(command.key) ?: throw KeyNotSetException()
            is Command.Set -> store.set(command.key, command.value)
            is Command.Delete -> store.delete(command.key) ?: throw KeyNotSetException()
            is Command.Count -> return store.count(command.value).toString()
            is Command.Begin -> store.beginTransaction()
            is Command.Commit -> store.commitTransaction()
            is Command.Rollback -> store.rollbackTransaction()
        }
        return null
    }
}