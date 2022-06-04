package io.chthonic.storeminal.domain.model

import kotlin.reflect.KClass

sealed class Command {
    data class Get(val key: String) : Command()
    data class Set(val key: String, val value: String) : Command()
    data class Delete(val key: String) : Command()
    data class Count(val value: String) : Command()
    object Begin : Command()
    object Commit : Command()
    object Rollback : Command()

    companion object {
        val tokenNameMap: Map<KClass<out Command>, String> = mapOf(
            Get::class to "GET",
            Set::class to "SET",
            Delete::class to "DELETE",
            Count::class to "COUNT",
            Begin::class to "BEGIN",
            Commit::class to "COMMIT",
            Rollback::class to "ROLLBACK"
        )
    }
}