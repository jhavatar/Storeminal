package io.chthonic.storeminal.domain

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

class ParseCommandUseCase @Inject constructor() {

    fun execute(inputString: InputString): Command {
        val tokens = inputString.text.split(WHITE_SPACE)
        return parse(tokens)
    }

    private fun parse(tokens: List<String>): Command {
        if (tokens.isEmpty() || (tokens.size > MAX_EXPECTED_TOKENS)) {
            throw UnknownCommandException()
        }
        val commandToken = tokens[0]
        val paramToken1 = tokens.getOrNull(1)
        val paramToken2 = tokens.getOrNull(2)

        return if (paramToken1.isNullOrEmpty() && paramToken2.isNullOrEmpty()) {
            when {
                commandToken.isCommand(Begin::class) -> Begin
                commandToken.isCommand(Commit::class) -> Commit
                commandToken.isCommand(Rollback::class) -> Rollback
                else -> throw UnknownCommandException()
            }
        } else if (paramToken1.isNotNullOrEmpty() && paramToken2.isNullOrEmpty()) {
            when {
                commandToken.isCommand(Get::class) -> Get(paramToken1)
                commandToken.isCommand(Delete::class) -> Delete(paramToken1)
                commandToken.isCommand(Count::class) -> Count(paramToken1)
                else -> throw UnknownCommandException()
            }
        } else if (paramToken1.isNotNullOrEmpty() && paramToken2.isNotNullOrEmpty()) {
            when {
                commandToken.isCommand(Set::class) -> Set(paramToken1, paramToken2)
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