package io.chthonic.storeminal.domain.model

import io.chthonic.storeminal.domain.error.UnknownCommandException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private const val GET = "GET"
private const val SET = "SET"
private const val DELETE = "DELETE"
private const val COUNT = "COUNT"
private const val BEGIN = "BEGIN"
private const val COMMIT = "COMMIT"
private const val ROLLBACK = "ROLLBACK"

private val WHITE_SPACE = Regex("""\s+""")

sealed class Command() {
    class Get(val key: String) : Command()
    class Set(val key: String, val value: String) : Command()
    class Delete(val key: String) : Command()
    class Count(val value: String) : Command()
    object Begin : Command()
    object Commit : Command()
    object Rollback : Command()

    companion object {
        fun parse(input: String): Command {
            val words = input.trim().split(WHITE_SPACE)
            return parse(words)
        }

        private fun parse(words: List<String>): Command {
            if (words.isEmpty() || (words.size > 3)) {
                throw UnknownCommandException()
            }
            val commandWord = words[0]
            val param1 = words.getOrNull(1)
            val param2 = words.getOrNull(2)

            return if (param1.isNullOrEmpty() && param2.isNullOrEmpty()) {
                when {
                    commandWord.isCommand(BEGIN) -> Begin
                    commandWord.isCommand(COMMIT) -> Commit
                    commandWord.isCommand(ROLLBACK) -> Rollback
                    else -> throw UnknownCommandException()
                }
            } else if (param1.isNotNullOrEmpty() && param2.isNullOrEmpty()) {
                when {
                    commandWord.isCommand(GET) -> Get(param1)
                    commandWord.isCommand(DELETE) -> Delete(param1)
                    commandWord.isCommand(COUNT) -> Count(param1)
                    else -> throw UnknownCommandException()
                }
            } else if (param1.isNotNullOrEmpty() && param2.isNotNullOrEmpty()) {
                when {
                    commandWord.isCommand(SET) -> Set(param1, param2)
                    else -> throw UnknownCommandException()
                }
            } else {
                throw UnknownCommandException()
            }
        }
    }
}

private fun String.isCommand(commandName: String): Boolean =
    this.equals(commandName, ignoreCase = true)

@OptIn(ExperimentalContracts::class)
private inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}