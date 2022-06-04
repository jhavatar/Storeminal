package io.chthonic.storeminal.domain

import android.util.Log
import io.chthonic.storeminal.domain.api.KeyValueStore
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.model.Command
import io.chthonic.storeminal.domain.model.InputString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExecuteCommandUseCase @Inject constructor(
    private val api: KeyValueStore
) {
    suspend fun execute(input: InputString): String? {
        Log.v("d3V", "execute input = $input")
        val command = withContext(Dispatchers.Default) {
            Command.parse(input.text)
        }
        Log.v("d3V", "execute command = $command")
        return executeCommand(command)
    }

    private suspend fun executeCommand(command: Command): String? {
        when (command) {
            is Command.Get -> return api.get(command.key) ?: throw KeyNotSetException()
            is Command.Set -> api.set(command.key, command.value)
            is Command.Delete -> api.delete(command.key) ?: throw KeyNotSetException()
            is Command.Count -> return api.count(command.value).toString()
            is Command.Begin -> api.beginTransaction()
            is Command.Commit -> api.commitTransaction()
            is Command.Rollback -> api.rollbackTransaction()
        }
        return null
    }
}