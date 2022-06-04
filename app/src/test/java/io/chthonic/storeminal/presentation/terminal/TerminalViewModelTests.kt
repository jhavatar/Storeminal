package io.chthonic.storeminal.presentation.terminal

import io.chthonic.storeminal.domain.api.NoTransactionException
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString
import io.chthonic.storeminal.domain.usecase.ExecuteCommandLineInputUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val COLOR_CHEVRON = "#FAFA91"
private const val COLOR_ERROR = "#FFB0E5"
private const val COLOR_NON_ERROR = "#9FFF99"

class TerminalViewModelTests {
    val input = InputString.validateOrNull("get foo")!!

    val executeCommandLineInputUseCase: ExecuteCommandLineInputUseCase = mock()
    val tested = TerminalViewModel(executeCommandLineInputUseCase)

    val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initial viewModel state then inputSubmitEnabled is true, clearInput is false and history is empty `() =
        runTest {
            // when / then
            tested.inputSubmitEnabled.value.shouldBeTrue()
            tested.clearInput.value.shouldBeFalse()
            tested.historyToDisplay.value.shouldBeEqualTo("")
        }

    @Test
    fun `given inputEnabled is false when onInputSubmitted then ignore input`() = runTest {
        // given
        tested._inputSubmitEnabled.value = false

        // when
        tested.onInputSubmitted(input)

        // then
        verify(executeCommandLineInputUseCase, never()).execute(input)
    }

    @Test
    fun `when onInputSubmitted then update history with input`() = runTest {
        // when
        tested.onInputSubmitted(input)

        // then
        tested.historyToDisplay.value.shouldBeEqualTo("<font color='$COLOR_CHEVRON'>></font> get foo")
    }

    @Test
    fun `when onInputSubmitted then clearInput set to true`() = runTest {
        // when
        tested.onInputSubmitted(input)

        // then
        tested.clearInput.value.shouldBeTrue()
    }

    @Test
    fun `when onInputSubmitted then attempt to execute input as a command`() = runTest {
        // when
        tested.onInputSubmitted(input)

        // then
        verify(executeCommandLineInputUseCase).execute(input)
    }

    @Test
    fun `when onInputCleared then reset clearInput to false`() = runTest {
        // given
        tested._clearInput.value = true

        // when
        tested.onInputCleared()

        // then
        tested._clearInput.value.shouldBeFalse()
    }

    @Test
    fun `when onInputSubmitted executes command thatreturns response then add expected success response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(input)).thenReturn("bar")

            // when
            tested.onInputSubmitted(input)

            // then
            tested.historyToDisplay.value.endsWith("<font color=$COLOR_NON_ERROR>bar</font>")
        }

    @Test
    fun `when onInputSubmitted executes command that throws UnknownCommandException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(input)).thenThrow(
                UnknownCommandException()
            )

            // when
            tested.onInputSubmitted(input)

            // then
            tested.historyToDisplay.value.endsWith("<font color=$COLOR_ERROR>unknown command</font>")
        }

    @Test
    fun `when onInputSubmitted executes command that throws NoTransactionException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(input)).thenThrow(NoTransactionException())

            // when
            tested.onInputSubmitted(input)

            // then
            tested.historyToDisplay.value.endsWith("<font color=$COLOR_ERROR>no transaction</font>")
        }

    @Test
    fun `when onInputSubmitted executes command that throws KeyNotSetException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(input)).thenThrow(KeyNotSetException())

            // when
            tested.onInputSubmitted(input)

            // then
            tested.historyToDisplay.value.endsWith("<font color=$COLOR_ERROR>key not set</font>")
        }
}