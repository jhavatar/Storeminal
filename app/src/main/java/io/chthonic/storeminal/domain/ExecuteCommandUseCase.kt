package io.chthonic.storeminal.domain

import io.chthonic.storeminal.domain.api.KeyValueStore
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.model.Command
import io.chthonic.storeminal.domain.model.InputString
import javax.inject.Inject

class ExecuteCommandUseCase @Inject constructor(
    private val parseCommandUseCase: ParseCommandUseCase,
    private val store: KeyValueStore
) {
    suspend fun execute(input: InputString): String? {
//        Log.v("d3V", "execute input = $input")
        val command = parseCommandUseCase.execute(input)
//        Log.v("d3V", "execute command = $command")
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