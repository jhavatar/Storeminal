package io.chthonic.storeminal.data.store.memory

import io.chthonic.storeminal.domain.api.NoTransactionException
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class MemoryStoreTests {

    val tested: MemoryStore = MemoryStore()

    @Test
    fun `given key does not exist when get then return null`() {
        // given/when/then
        tested.get("foo").shouldBeNull()
    }

    @Test
    fun `given key does exists when get then return value`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)

        // when/then
        tested.get("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does not exist when set key to value then return said value`() {
        // when
        tested.set("foo", "bar")

        // then
        tested.get("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does exist when set key to value when get then return new value`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)

        // when
        tested.set("foo", "ro")

        // then
        tested.get("foo").shouldBeEqualTo("ro")
    }

    @Test
    fun `given key does not exist when delete then return null`() {
        // given/when/then
        tested.delete("foo").shouldBeNull()
    }

    @Test
    fun `given key does exists when delete then return value`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)

        // when/then
        tested.delete("foo").shouldBeEqualTo("bar")
    }

    @Test
    fun `given key does exists when delete then key no longer exists`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)

        // when/then
        tested.delete("foo")
        tested.get("foo").shouldBeNull()
    }

    @Test
    fun `given value does not exist when count value then return 0`() {
        // when/then
        tested.count("bar").shouldBeEqualTo(0)
    }

    @Test
    fun `given value does exist multiple times when count value then return expected count`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        map["fus"] = "bar"
        val tested = buildTested(map)

        // when/then
        tested.count("bar").shouldBeEqualTo(2)
    }

    @Test(expected = Test.None::class)
    fun `when beginTransaction is called multiple times then there is no exception thrown`() {
        // when/then
        tested.beginTransaction()
        tested.beginTransaction()
        tested.beginTransaction()
    }

    @Test
    fun `given key does exist when beginTransaction then key does not exist in active transaction`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)

        // when
        tested.beginTransaction()

        // when/then
        tested.get("bar").shouldBeNull()
    }

    @Test(expected = NoTransactionException::class)
    fun `given no started transactions when commitTransaction then throw NoTransactionException`() {
        // when/then
        tested.commitTransaction()
    }

    @Test(expected = Test.None::class)
    fun `given a started transactions when commitTransaction throw then no exception thrown`() {
        // given
        tested.beginTransaction()

        // when/then
        tested.commitTransaction()
    }

    @Test
    fun `given an existing key followed by beginTransaction() and set new value for the same key when commitTransaction then return the new value for the key`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)
        tested.beginTransaction()
        tested.set("foo", "ro")

        // when
        tested.commitTransaction()

        // then
        tested.get("foo").shouldBeEqualTo("ro")
    }

    @Test(expected = NoTransactionException::class)
    fun `given no started transactions when rollbackTransaction then throw NoTransactionException`() {
        // when/then
        tested.rollbackTransaction()
    }

    @Test(expected = Test.None::class)
    fun `given a started transactions when rollbackTransaction then throw no exceptions`() {
        // given
        tested.beginTransaction()

        // when/then
        tested.rollbackTransaction()
    }

    @Test
    fun `given an existing key followed by beginTransaction() and set new value for the same key when rollbackTransaction then return the original value for the key`() {
        // given
        val map = ConcurrentHashMap<String, String>()
        map["foo"] = "bar"
        val tested = buildTested(map)
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

    private fun buildTested(vararg maps: MutableMap<String, String>): MemoryStore =
        MemoryStore(buildStack(*maps))
}