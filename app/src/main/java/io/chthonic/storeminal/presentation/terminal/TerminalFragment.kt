package io.chthonic.storeminal.presentation.terminal

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.chthonic.storeminal.databinding.TerminalFragmentBinding
import io.chthonic.storeminal.domain.model.InputString
import kotlinx.coroutines.flow.filter

private const val NEW_LINE = "\n"

@AndroidEntryPoint
class TerminalFragment : Fragment() {

    companion object {
        fun newInstance() = TerminalFragment()
    }

    private lateinit var binding: TerminalFragmentBinding

    private val viewModel: TerminalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TerminalFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        collectUiState()
    }

    private fun initView() {
        binding.inputView.requestFocus()

        binding.submitButton.setOnClickListener {
            InputString.validateOrNull(binding.inputView.text)?.let { inputString ->
                viewModel.onInputSubmitted(inputString)
            }
        }

        binding.inputView.addTextChangedListener {
            if (it?.endsWith(NEW_LINE) == true) {
                InputString.validateOrNull(it)?.let { inputString ->
                    viewModel.onInputSubmitted(inputString)
                }
            }
        }
    }

    private fun collectUiState() {
        lifecycleScope.launchWhenStarted {
            viewModel.historyToDisplay.collect { history ->
                binding.historyView.text = Html.fromHtml(history, Html.FROM_HTML_MODE_COMPACT)
                binding.historyScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.clearInput.filter { it }.collect {
                binding.inputView.text.clear()
                viewModel.onInputCleared()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.inputEnabled.collect { enabled ->
                binding.submitButton.isEnabled = enabled
            }
        }
    }
}