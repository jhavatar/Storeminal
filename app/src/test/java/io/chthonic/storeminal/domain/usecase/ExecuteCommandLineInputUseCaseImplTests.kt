package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.api.ConcurrentKeyValueStore
import io.chthonic.storeminal.domain.api.NoTransactionException
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.Command.*
import io.chthonic.storeminal.domain.model.Command.Set
import io.chthonic.storeminal.domain.model.InputString
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class ExecuteCommandLineInputUseCaseImplTests {
    val getInput = InputString.validateOrNull("get foo")!!
    val setInput = InputString.validateOrNull("set foo bar")!!
    val deleteInput = InputString.validateOrNull("delete foo")!!
    val countInput = InputString.validateOrNull("count foo")!!
    val beginInput = InputString.validateOrNull("begin")!!
    val commitInput = InputString.validateOrNull("commit")!!
    val rollbackInput = InputString.validateOrNull("rollback")!!

    val getCommand = Get("foo")
    val setCommand = Set("foo", "bar")
    val deleteCommand = Delete("foo")
    val countCommand = Count("foo")
    val beginCommand = Begin
    val commitCommand = Commit
    val rollbackCommand = Rollback

    val store: ConcurrentKeyValueStore = mock {
        on { runBlocking { get("foo") } } doReturn "bar"
        on { runBlocking { delete("foo") } } doReturn "bar"
        on { runBlocking { count("foo") } } doReturn 1
    }
    val parseCommandUseCase: ParseCommandUseCase = mock {
        on { execute(getInput) } doReturn getCommand
        on { execute(setInput) } doReturn setCommand
        on { execute(deleteInput) } doReturn deleteCommand
        on { execute(countInput) } doReturn countCommand
        on { execute(beginInput) } doReturn beginCommand
        on { execute(commitInput) } doReturn commitCommand
        on { execute(rollbackInput) } doReturn rollbackCommand
    }
    val tested = ExecuteCommandLineInputUseCaseImpl(parseCommandUseCase, store)

    @Test
    fun `given valid Get input when execute then execute expected get on store`() = runTest {
        // when
        tested.execute(getInput)

        // then
        verify(store).get("foo")
    }

    @Test
    fun `given valid Set input when execute then execute expected set on store`() = runTest {
        // when
        tested.execute(setInput)

        // then
        verify(store).set("foo", "bar")
    }

    @Test
    fun `given valid Delete input when execute then execute expected set on store`() = runTest {
        // when
        tested.execute(deleteInput)

        // then
        verify(store).delete("foo")
    }

    @Test
    fun `given valid Count input when execute then execute expected count on store`() = runTest {
        // when
        tested.execute(countInput)

        // then
        verify(store).count("foo")
    }

    @Test
    fun `given valid Begin input when execute then execute expected beginTransaction on store`() =
        runTest {
            // when
            tested.execute(beginInput)

            // then
            verify(store).beginTransaction()
        }

    @Test
    fun `given valid Commit input when execute then execute expected commitTransaction on store`() =
        runTest {
            // when
            tested.execute(commitInput)

            // then
            verify(store).commitTransaction()
        }

    @Test
    fun `given valid Rollback input when execute then execute expected rollbackTransaction on store`() =
        runTest {
            // when
            tested.execute(rollbackInput)

            // then
            verify(store).rollbackTransaction()
        }

    @Test(expected = KeyNotSetException::class)
    fun `given store returns null for get call when execute then throw KeyNotSetException`() =
        runTest {
            // given
            whenever(store.get("foo")).thenReturn(null)

            // when/then
            tested.execute(getInput)
        }

    @Test(expected = KeyNotSetException::class)
    fun `given store returns null for delete call when execute then throw KeyNotSetException`() =
        runTest {
            // given
            whenever(store.delete("foo")).thenReturn(null)

            // when/then
            tested.execute(deleteInput)
        }

    @Test(expected = NoTransactionException::class)
    fun `given store returns NoTransactionException for commitTransaction call when execute then throw NoTransactionException`() =
        runTest {
            // given
            whenever(store.commitTransaction()).thenThrow(NoTransactionException())

            // when/then
            tested.execute(commitInput)
        }

    @Test(expected = NoTransactionException::class)
    fun `given store returns NoTransactionException for rollbackTransaction call when execute then throw NoTransactionException`() =
        runTest {
            // given
            whenever(store.rollbackTransaction()).thenThrow(NoTransactionException())

            // when/then
            tested.execute(rollbackInput)
        }

    @Test(expected = UnknownCommandException::class)
    fun `given parseCommandUseCase returns UnknownCommandException when execute then throw UnknownCommandException`() =
        runTest {
            // given
            whenever(parseCommandUseCase.execute(getInput)).thenThrow(UnknownCommandException())

            // when/then
            tested.execute(getInput)
        }
}