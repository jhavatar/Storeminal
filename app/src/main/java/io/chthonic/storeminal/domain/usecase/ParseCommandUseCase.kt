package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.Command
import io.chthonic.storeminal.domain.model.Command.*
import io.chthonic.storeminal.domain.model.Command.Set
import io.chthonic.storeminal.domain.model.InputString
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

private val WHITE_SPACE = Regex("""\s+""")
private const val MAX_EXPECTED_TOKENS = 3

internal class ParseCommandUseCase @Inject constructor() {

    fun execute(inputString: InputString): Command {
        val tokens = inputString.text.split(WHITE_SPACE)
        return parse(tokens)
    }

    private fun parse(tokens: List<String>): Command {
        if (tokens.isEmpty() || (tokens.size > MAX_EXPECTED_TOKENS)) {
            throw UnknownCommandException()
        }
        val commandToken = tokens[0]
        val param1Token = tokens.getOrNull(1)
        val param2Token = tokens.getOrNull(2)

        return if (param1Token.isNullOrEmpty() && param2Token.isNullOrEmpty()) {
            when {
                commandToken.isCommand(Begin::class) -> Begin
                commandToken.isCommand(Commit::class) -> Commit
                commandToken.isCommand(Rollback::class) -> Rollback
                else -> throw UnknownCommandException()
            }
        } else if (param1Token.isNotNullOrEmpty() && param2Token.isNullOrEmpty()) {
            when {
                commandToken.isCommand(Get::class) -> Get(param1Token)
                commandToken.isCommand(Delete::class) -> Delete(param1Token)
                commandToken.isCommand(Count::class) -> Count(param1Token)
                else -> throw UnknownCommandException()
            }
        } else if (param1Token.isNotNullOrEmpty() && param2Token.isNotNullOrEmpty()) {
            when {
                commandToken.isCommand(Set::class) -> Set(param1Token, param2Token)
                else -> throw UnknownCommandException()
            }
        } else {
            throw UnknownCommandException()
        }
    }
}

private fun String.isCommand(commandClass: KClass<out Command>): Boolean =
    this.equals(Command.tokenNameMap[commandClass], ignoreCase = true)

@OptIn(ExperimentalContracts::class)
private inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}