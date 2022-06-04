package io.chthonic.storeminal.presentation.terminal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.chthonic.storeminal.domain.ExecuteCommandUseCase
import io.chthonic.storeminal.domain.api.NoTransactionException
import io.chthonic.storeminal.domain.error.KeyNotSetException
import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.InputString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNKNOWN_COMMAND = "unknown command"
private const val NO_TRANS = "no transaction"
private const val KEY_NOT_SET = "key not set"

private const val COLOR_ERROR = "#CC0000"
private const val COLOR_NON_ERROR = "#4BB543"

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val executeCommandUseCase: ExecuteCommandUseCase
) : ViewModel() {
    private val history: MutableList<HistoryItem> = mutableListOf()

    private val _historyToDisplay = MutableStateFlow("")
    val historyToDisplay: StateFlow<String> = _historyToDisplay.asStateFlow()

    private val _clearInput = MutableStateFlow(false)
    val clearInput: StateFlow<Boolean> = _clearInput.asStateFlow()

    private val _inputEnabled = MutableStateFlow(true)
    val inputEnabled: StateFlow<Boolean> = _inputEnabled.asStateFlow()

    fun onInputSubmitted(input: InputString) {
        if (!inputEnabled.value) return
        _clearInput.value = true
        _inputEnabled.value = false
        updateHistory(HistoryItem.InputHistory(input.text))
        executeCommand(input)
    }

    fun onInputCleared() {
        _clearInput.value = false
    }

    private fun executeCommand(input: InputString) {
        viewModelScope.launch {
            try {
                val response = executeCommandUseCase.execute(input)
                if (response != null) {
                    updateHistory(HistoryItem.OutputHistory(response, isError = false))
                }
                Log.v("d3V", "executeCommandUseCase success $response")
            } catch (e: UnknownCommandException) {
                updateHistory(HistoryItem.OutputHistory(UNKNOWN_COMMAND))
            } catch (e: NoTransactionException) {
                updateHistory(HistoryItem.OutputHistory(NO_TRANS))
            } catch (e: KeyNotSetException) {
                updateHistory(HistoryItem.OutputHistory(KEY_NOT_SET))
            } catch (e: Exception) {
                Log.e("D3V", "executeCommandUseCase failed", e)
            }
            _inputEnabled.value = true
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

sealed class HistoryItem(val text: String) {
    class InputHistory(text: String) : HistoryItem(text) {
        override fun toString(): String = "> $text"
    }

    class OutputHistory(text: String, val isError: Boolean = true) : HistoryItem(text) {
        override fun toString(): String {
            val color = if (isError) COLOR_ERROR else COLOR_NON_ERROR
            return "<font color='$color'>$text</font>"
        }
    }
}