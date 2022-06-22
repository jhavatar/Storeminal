package io.chthonic.storeminal.data.store.concurrent

import io.chthonic.storeminal.data.store.memory.MemoryStore
import io.chthonic.storeminal.data.store.memory.generateValueCounters
import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import io.chthonic.storeminal.domain.api.NoTransactionException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal abstract class ConcurrentStoreIntegrationTestsTemplate {

    private val defaultTested: ConcurrentKeyValueStore by lazy {
        buildTested(MemoryStore())
    }

    abstract fun buildTested(coreStore: MemoryStore): ConcurrentKeyValueStore

    @Test
    fun `given key does not exist when get then return null`() = runTest {
        // given/when/then
        defaultTested.get("foo").shouldBeNull()
    }

    @Test
    fun `given key does exists when get then return value`() = runTest {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(buildCoreStore(map))

        // when/then
        tested.get("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does not exist when set key to value then return said value`() = runTest {
        // when
        defaultTested.set("foo", "bar")

        // then
        defaultTested.get("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does exist when set key to value when get then return new value`() = runTest {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(buildCoreStore(map))

        // when
        tested.set("foo", "ro")

        // then
        tested.get("foo").shouldBeEqualTo("ro")
    }

    @Test
    fun `given key does not exist when delete then return null`() = runTest {
        // given/when/then
        defaultTested.delete("foo").shouldBeNull()
    }

    @Test
    fun `given key does exists when delete then return value`() = runTest {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(buildCoreStore(map))

        // when/then
        tested.delete("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does exists when delete then key no longer exists`() = runTest {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(buildCoreStore(map))

        // when/then
        tested.delete("foo")
        tested.get("foo").shouldBeNull()
    }

    @Test
    fun `given value does not exist when count value then return 0`() = runTest {
        // when/then
        defaultTested.count("bar").shouldBeEqualTo(0)
    }

    @Test
    fun `given value does exist multiple times when count value then return expected count`() =
        runTest {
            // given
            val map = ConcurrentHashMap<String, String>()
            map["foo"] = "bar"
            map["fus"] = "bar"
            val tested = buildTested(buildCoreStore(map))

            // when/then
            tested.count("bar").shouldBeEqualTo(2)
        }

    @Test(expected = Test.None::class)
    fun `when beginTransaction is called multiple times then there is no exception thrown`() =
        runTest {
            // when/then
            defaultTested.beginTransaction()
            defaultTested.beginTransaction()
            defaultTested.beginTransaction()
        }

    @Test
    fun `given key does exist when beginTransaction then key does not exist in active transaction`() =
        runTest {
            // given
            val map = ConcurrentHashMap<String, String>()
            map["foo"] = "bar"
            val tested = buildTested(buildCoreStore(map))

            // when
            tested.beginTransaction()

            // when/then
            tested.get("bar").shouldBeNull()
        }

    @Test(expected = NoTransactionException::class)
    fun `given no started transactions when commitTransaction then throw NoTransactionException`() =
        runTest {
            // when/then
            defaultTested.commitTransaction()
        }

    @Test(expected = Test.None::class)
    fun `given a started transactions when commitTransaction throw then no exception thrown`() =
        runTest {
            // given
            defaultTested.beginTransaction()

            // when/then
            defaultTested.commitTransaction()
        }

    @Test
    fun `given an existing key followed by beginTransaction() and set new value for the same key when commitTransaction then return the new value for the key`() =
        runTest {
            // given
            val map = ConcurrentHashMap<String, String>()
            map["foo"] = "bar"
            val tested = buildTested(buildCoreStore(map))
            tested.beginTransaction()
            tested.set("foo", "ro")

            // when
            tested.commitTransaction()

            // then
            tested.get("foo").shouldBeEqualTo("ro")
        }

    @Test(expected = NoTransactionException::class)
    fun `given no started transactions when rollbackTransaction then throw NoTransactionException`() =
        runTest {
            // when/then
            defaultTested.rollbackTransaction()
        }

    @Test(expected = Test.None::class)
    fun `given a started transactions when rollbackTransaction then throw no exceptions`() =
        runTest {
            // given
            defaultTested.beginTransaction()

            // when/then
            defaultTested.rollbackTransaction()
        }

    @Test
    fun `given an existing key followed by beginTransaction() and set new value for the same key when rollbackTransaction then return the original value for the key`() =
        runTest {
            // given
            val map = ConcurrentHashMap<String, String>()
            map["foo"] = "bar"
            val tested = buildTested(buildCoreStore(map))
            tested.beginTransaction()
            tested.set("foo", "ro")

            // when
            tested.rollbackTransaction()

            // then
            tested.get("foo").shouldBeEqualTo("bar")
        }

    private fun buildStack(
        vararg maps: MutableMap<String, String>
    ): ArrayDeque<MemoryStore.Transaction> =
        ArrayDeque<MemoryStore.Transaction>(maps.size).apply {
            maps.forEach { map ->
                val trans = MemoryStore.Transaction(
                    map = map,
                    valueCounters = map.generateValueCounters()
                )
                addLast(trans)
            }
        }

    private fun buildCoreStore(vararg maps: MutableMap<String, String>): MemoryStore =
        MemoryStore(buildStack(*maps))
}