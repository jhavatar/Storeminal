package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.Command
import io.chthonic.storeminal.domain.model.Command.*
import io.chthonic.storeminal.domain.model.Command.Set
import io.chthonic.storeminal.domain.model.CommandToken
import io.chthonic.storeminal.domain.model.CommandToken.*
import io.chthonic.storeminal.domain.model.InputString
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

        return when {
            param1Token.isNotNullOrEmpty() && param2Token.isNotNullOrEmpty() -> parseThreeTokenCommands(
                commandToken,
                param1Token,
                param2Token
            )

            param1Token.isNotNullOrEmpty() -> parseTwoTokenCommands(commandToken, param1Token)
            else -> parseOneTokenCommands(commandToken)
        }
    }

    private fun parseThreeTokenCommands(
        commandToken: String,
        param1Token: String,
        param2Token: String
    ) = when {
        commandToken.isCommand(SET) -> Set(param1Token, param2Token)
        else -> throw UnknownCommandException()
    }

    private fun parseTwoTokenCommands(commandToken: String, param1Token: String) = when {
        commandToken.isCommand(GET) -> Get(param1Token)
        commandToken.isCommand(DELETE) -> Delete(param1Token)
        commandToken.isCommand(COUNT) -> Count(param1Token)
        else -> throw UnknownCommandException()
    }

    private fun parseOneTokenCommands(commandToken: String) = when {
        commandToken.isCommand(BEGIN) -> Begin
        commandToken.isCommand(COMMIT) -> Commit
        commandToken.isCommand(ROLLBACK) -> Rollback
        else -> throw UnknownCommandException()
    }
}

private fun String.isCommand(commandToken: CommandToken): Boolean =
    this.equals(commandToken.value, ignoreCase = true)

@OptIn(ExperimentalContracts::class)
private inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}