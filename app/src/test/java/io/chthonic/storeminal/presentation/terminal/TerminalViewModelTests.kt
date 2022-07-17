package io.chthonic.storeminal.presentation.terminal

import io.chthonic.storeminal.domain.api.NoTransactionException
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString
import io.chthonic.storeminal.domain.usecase.ExecuteCommandLineInputUseCase
import io.chthonic.storeminal.presentation.terminal.TerminalViewModel.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class TerminalViewModelTests {
    val inputTextToDisplay = "get foo"
    val inputString = InputString.validateOrNull("get foo")!!
    val inputHistory = listOf(HistoryItem.InputHistory("get foo"))

    val executeCommandLineInputUseCase: ExecuteCommandLineInputUseCase = mock()

    val tested = TerminalViewModel(
        executeCommandLineInputUseCase,
        State().copy(
            inputTextToDisplay = inputTextToDisplay
        )
    )

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
    fun `when get initial viewModel state then state is the default state`() =
        runTest {
            // when / then
            TerminalViewModel(executeCommandLineInputUseCase).state.value.shouldBeEqualTo(State())
        }

    @Test
    fun `given valid input text and inputEnabled is false when onInputSubmitted then ignore input and not execute input`() =
        runTest {
            // given
            val tested = TerminalViewModel(
                executeCommandLineInputUseCase, State().copy(
                    inputTextToDisplay = inputTextToDisplay,
                    inputSubmitEnabled = false
                )
            )

            // when
            tested.onInputSubmitted()

            // then
            verify(executeCommandLineInputUseCase, never()).execute(inputString)
        }

    @Test
    fun `given invalid input and inputEnabled is true text when onInputSubmitted then ignore input and state not updated`() =
        runTest {
            // given
            val state = State().copy(
                inputTextToDisplay = "",
                inputSubmitEnabled = true,
                history = inputHistory
            )
            val tested = TerminalViewModel(executeCommandLineInputUseCase, state)

            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.shouldBeEqualTo(state)
        }

    @Test
    fun `given valid input text when onInputSubmitted then add input to history`() = runTest {
        // when
        tested.onInputSubmitted()

        // then
        tested.state.value.history.toString().shouldBeEqualTo(inputHistory.toString())
    }

    @Test
    fun `given valid input text when onInputSubmitted then update history with input`() = runTest {
        // when
        tested.onInputSubmitted()

        // then
        tested.state.value.historyToDisplay.shouldBeEqualTo("<font color='$COLOR_CHEVRON'>></font> get foo")
    }

    @Test
    fun `given valid input when onInputSubmitted then text to display should be cleared`() =
        runTest {
            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.inputTextToDisplay.shouldBeEmpty()
        }


    @Test
    fun `given valid input when onInputSubmitted then attempt to execute input as a command`() =
        runTest {
            // when
            tested.onInputSubmitted()

            // then
            verify(executeCommandLineInputUseCase).execute(inputString)
        }

    @Test
    fun `given valid input when onInputSubmitted executes command that returns response then add expected success response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(inputString)).thenReturn("bar")

            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.historyToDisplay.endsWith("<font color=$COLOR_NON_ERROR>bar</font>")
        }

    @Test
    fun `given valid input when onInputSubmitted executes command that throws UnknownCommandException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(inputString)).thenThrow(
                UnknownCommandException()
            )

            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.historyToDisplay.endsWith("<font color=$COLOR_ERROR>unknown command</font>")
        }

    @Test
    fun `given valid input when onInputSubmitted executes command that throws NoTransactionException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(inputString)).thenThrow(
                NoTransactionException()
            )

            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.historyToDisplay.endsWith("<font color=$COLOR_ERROR>no transaction</font>")
        }

    @Test
    fun `given valid input when onInputSubmitted executes command that throws KeyNotSetException exception then add expected error response to history`() =
        runTest {
            // given
            whenever(executeCommandLineInputUseCase.execute(inputString)).thenThrow(
                KeyNotSetException()
            )

            // when
            tested.onInputSubmitted()

            // then
            tested.state.value.historyToDisplay.endsWith("<font color=$COLOR_ERROR>key not set</font>")
        }
}