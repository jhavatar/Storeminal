package io.chthonic.storeminal.data.store.memory

import io.chthonic.storeminal.domain.api.KeyValueStore
import io.chthonic.storeminal.domain.api.NoTransactionException

/**
 * Implementation of [KeyValueStore] that only exists in memory.
 * @param initialState The initial state of the store, default is null. Can be used for testing and to restore the store from persistence.
 */
internal class MemoryStore(initialState: ArrayDeque<Transaction>? = null) : KeyValueStore {
    private val transStack =
        initialState ?: ArrayDeque<Transaction>(1).apply {
            this.addLast(Transaction())
        }

    override fun get(key: String): String? =
        getActiveTrans().map[key]

    override fun set(key: String, value: String) {
        setBatch(listOf(Pair(key, value)))
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

    override fun delete(key: String): String? {
        val activeTrans = getActiveTrans()
        return activeTrans.map.remove(key)?.also { oldVal ->
            activeTrans.valueCounters[oldVal]?.dec()
        }
    }

    override fun count(value: String): Int =
        getActiveTrans().valueCounters[value]?.count ?: 0

    override fun beginTransaction() {
        transStack.addLast(Transaction())
    }

    override fun commitTransaction() {
        val oldTrans = removeActiveTrans() ?: throw NoTransactionException()
        setBatch(oldTrans.map.entries.map {
            Pair(it.key, it.value)
        })
    }

    override fun rollbackTransaction() {
        removeActiveTrans() ?: throw NoTransactionException()
    }

    private fun getActiveTrans(): Transaction =
        transStack.last()

    private fun removeActiveTrans(): Transaction? =
        if (transStack.size > 1) {
            transStack.removeLast()
        } else null

    private fun MutableMap<String, Counter>.getNewlyCreatedAndAddedCounter(
        value: String
    ): Counter =
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

internal fun Map<String, String>.generateValueCounters(): MutableMap<String, MemoryStore.Counter> {
    val valueCounters: MutableMap<String, MemoryStore.Counter> = mutableMapOf()
    this.values.toSet().forEach { value ->
        valueCounters[value] = this.count {
            it.value == value
        }.toCounter()
    }
    return valueCounters
}

private fun Int.toCounter(): MemoryStore.Counter =
    MemoryStore.Counter(this)