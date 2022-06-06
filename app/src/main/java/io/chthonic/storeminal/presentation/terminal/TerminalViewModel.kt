package io.chthonic.storeminal.presentation.terminal

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.chthonic.storeminal.domain.api.NoTransactionException
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString
import io.chthonic.storeminal.domain.usecase.ExecuteCommandLineInputUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNKNOWN_COMMAND = "unknown command"
private const val NO_TRANS = "no transaction"
private const val KEY_NOT_SET = "key not set"

private const val COLOR_CHEVRON = "#FAFA91"
private const val COLOR_ERROR = "#FFB0E5"
private const val COLOR_NON_ERROR = "#9FFF99"

@HiltViewModel
internal class TerminalViewModel @Inject constructor(
    private val executeCommandLineInputUseCase: ExecuteCommandLineInputUseCase
) : ViewModel() {
    private val history: MutableList<HistoryItem> = mutableListOf()

    private val _historyToDisplay = MutableStateFlow("")
    val historyToDisplay: StateFlow<String> = _historyToDisplay.asStateFlow()

    @VisibleForTesting
    val _clearInput = MutableStateFlow(false)
    val clearInput: StateFlow<Boolean> = _clearInput.asStateFlow()

    @VisibleForTesting
    val _inputSubmitEnabled = MutableStateFlow(true)
    val inputSubmitEnabled: StateFlow<Boolean> = _inputSubmitEnabled.asStateFlow()

    fun onInputSubmitted(input: InputString) {
        if (!inputSubmitEnabled.value) return
        _clearInput.value = true
        _inputSubmitEnabled.value = false
        updateHistory(HistoryItem.InputHistory(input.text))
        executeCommandLineInput(input)
    }

    fun onInputCleared() {
        _clearInput.value = false
    }

    private fun executeCommandLineInput(input: InputString) {
        viewModelScope.launch {
            try {
                executeCommandLineInputUseCase.execute(input)?.let {
                    updateHistory(HistoryItem.OutputHistory(it, isError = false))
                }
            } catch (e: UnknownCommandException) {
                updateHistory(HistoryItem.OutputHistory(UNKNOWN_COMMAND))
            } catch (e: NoTransactionException) {
                updateHistory(HistoryItem.OutputHistory(NO_TRANS))
            } catch (e: KeyNotSetException) {
                updateHistory(HistoryItem.OutputHistory(KEY_NOT_SET))
            } catch (e: Exception) {
                Log.e("TerminalViewModel", "executeCommandUseCase failed", e)
            }
            _inputSubmitEnabled.value = true
        }
    }

    private fun updateHistory(historyItem: HistoryItem) {
        history.add(historyItem)
        _historyToDisplay.value =
            history.joinToString(separator = "<br>", transform = ::historyTransform)
    }

    private fun historyTransform(historyItem: HistoryItem): CharSequence =
        historyItem.toString()
}

private sealed class HistoryItem(val text: String) {
    class InputHistory(text: String) : HistoryItem(text) {
        override fun toString(): String = "<font color='$COLOR_CHEVRON'>></font> $text"
    }

    class OutputHistory(text: String, val isError: Boolean = true) : HistoryItem(text) {
        override fun toString(): String {
            val color = if (isError) COLOR_ERROR else COLOR_NON_ERROR
            return "<font color='$color'>$text</font>"
        }
    }
}