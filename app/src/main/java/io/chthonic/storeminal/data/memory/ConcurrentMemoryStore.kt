package io.chthonic.storeminal.data.memory

import io.chthonic.storeminal.domain.api.KeyValueStore
import io.chthonic.storeminal.domain.api.NoTransactionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Thread-safe in-memory implementation of [KeyValueStore].
 * @param initialState The initial state of the store, default is null. Can be used for testing and to restore the store from persistence.
 */
internal class ConcurrentMemoryStore(initialState: ArrayDeque<Transaction>? = null) :
    KeyValueStore {

    // Use semaphore with one permit as mutex since kotlin semaphore is fair and maintains a FIFO order of acquirers while kotlin mutex does not
    private val transMutex = Semaphore(permits = 1)
    private val transStack =
        initialState ?: ArrayDeque<Transaction>(1).apply {
            this.addLast(Transaction())
        }

    override suspend fun get(key: String): String? =
        transMutex.withPermit { getActiveTrans().map[key] }

    override suspend fun set(key: String, value: String) {
        transMutex.withPermit {
            setBatch(listOf(Pair(key, value)))
        }
    }

    private fun setBatch(entries: List<Pair<String, String>>) {
        val activeTrans = getActiveTrans()
        entries.forEach { (key, value) ->
            activeTrans.map[key]?.let { oldValue ->
                activeTrans.valueCounters[oldValue]?.dec()
            }
            activeTrans.map[key] = value
            (activeTrans.valueCounters[value]
                ?: activeTrans.valueCounters.getNewlyCreatedAndAddedCounter(value)).inc()
        }
    }

    override suspend fun delete(key: String): String? = transMutex.withPermit {
        val activeTrans = getActiveTrans()
        activeTrans.map.remove(key)?.also { oldVal ->
            activeTrans.valueCounters[oldVal]?.dec()
        }
    }

    override suspend fun count(value: String): Int = transMutex.withPermit {
        getActiveTrans().valueCounters[value]?.count ?: 0
    }

    override suspend fun beginTransaction() {
        transMutex.withPermit {
            transStack.addLast(Transaction())
        }
    }

    override suspend fun commitTransaction() {
        transMutex.withPermit {
            coroutineScope {
                // launch on background thread to not potentially block caller's thread since commit might take a while since the duration depends on the number of items in the transaction.
                launch(Dispatchers.Default) {
                    val oldTrans = removeActiveTrans() ?: throw NoTransactionException()
                    setBatch(oldTrans.map.entries.map {
                        Pair(it.key, it.value)
                    })
                }.join()
            }
        }
    }

    override suspend fun rollbackTransaction() {
        transMutex.withPermit {
            removeActiveTrans() ?: throw NoTransactionException()
        }
    }

    private fun getActiveTrans(): Transaction =
        transStack.last()

    private fun removeActiveTrans(): Transaction? =
        if (transStack.size > 1) {
            transStack.removeLast()
        } else null

    private fun MutableMap<String, Counter>.getNewlyCreatedAndAddedCounter(value: String): Counter =
        Counter().also { counter ->
            this[value] = counter
        }

    data class Counter(@Volatile private var internalCount: Int = 0) {
        val count: Int
            get() = internalCount

        fun inc() {
            internalCount++
        }

        fun dec() {
            internalCount--
        }
    }

    data class Transaction(
        val map: MutableMap<String, String> = mutableMapOf(),
        val valueCounters: MutableMap<String, Counter> = mutableMapOf()
    )
}

internal fun Map<String, String>.generateValueCounters(): MutableMap<String, ConcurrentMemoryStore.Counter> {
    val valueCounters: MutableMap<String, ConcurrentMemoryStore.Counter> = mutableMapOf()
    this.values.toSet().forEach { value ->
        valueCounters[value] = this.count {
            it.value == value
        }.toCounter()
    }
    return valueCounters
}

private fun Int.toCounter(): ConcurrentMemoryStore.Counter = ConcurrentMemoryStore.Counter(this)