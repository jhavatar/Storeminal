package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.domain.api.KeyValueStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription

/**
 * Sequentializes store input and output using two [MutableSharedFlow]s.
 */
internal class StoreFlowSequentializer(
    val store: KeyValueStore,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val input = MutableSharedFlow<Operation>()
    private val output = MutableSharedFlow<Response>()

    init {
        coroutineScope.launch {
            input.collect { op ->
                when (op) {
                    is Operation.Set -> performAndEmitCompletableOperation {
                        store.set(op.key, op.value)
                    }
                    is Operation.Get -> output.emit(Response.StringResponse(store.get(op.key)))
                    is Operation.Delete -> output.emit(
                        Response.StringResponse(store.delete(op.key))
                    )
                    is Operation.Count -> output.emit(
                        Response.IntResponse(store.count(op.value))
                    )
                    is Operation.Begin -> performAndEmitCompletableOperation {
                        store.beginTransaction()
                    }
                    is Operation.Commit -> performAndEmitCompletableOperation {
                        store.commitTransaction()
                    }
                    is Operation.Rollback -> performAndEmitCompletableOperation {
                        store.rollbackTransaction()
                    }
                }
            }
        }
    }

    private suspend fun performAndEmitCompletableOperation(block: () -> Unit) {
        try {
            block()
            output.emit(Response.Completed)
        } catch (e: Exception) {
            output.emit(Response.ExceptionResponse(e))
        }
    }

    suspend fun emit(op: Operation.Count): Response.IntResponse =
        emit(op as Operation) as Response.IntResponse

    suspend fun emit(op: Operation.Get): Response.StringResponse =
        emit(op as Operation) as Response.StringResponse

    suspend fun emit(op: Operation.Delete): Response.StringResponse =
        emit(op as Operation) as Response.StringResponse

    suspend fun emit(op: Operation): Response {
        var result: Response? = null
        try {
            coroutineScope {
                output.onSubscription {
                    input.emit(op)
                }.collect {
                    result = it
                    this.cancel()
                }
            }
        } catch (e: CancellationException) {
            // ignore expected exception
            requireNotNull(result).let {
                if (it is Response.ExceptionResponse) {
                    throw it.value
                }
                return it
            }
        }
    }

    sealed class Operation {
        data class Set(val key: String, val value: String) : Operation()
        data class Get(val key: String) : Operation()
        data class Delete(val key: String) : Operation()
        data class Count(val value: String) : Operation()
        object Begin : Operation()
        object Commit : Operation()
        object Rollback : Operation()
    }

    sealed class Response {
        data class StringResponse(val value: String?) : Response()
        data class IntResponse(val value: Int) : Response()
        data class ExceptionResponse(val value: Exception) : Response()
        object Completed : Response()
    }
}