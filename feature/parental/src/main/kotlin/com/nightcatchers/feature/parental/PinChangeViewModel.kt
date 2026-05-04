package com.nightcatchers.feature.parental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PinChangeStep {
    data object VerifyOld : PinChangeStep
    data object EnterNew : PinChangeStep
    data object ConfirmNew : PinChangeStep
}

data class PinChangeState(
    val digits: String = "",
    val step: PinChangeStep = PinChangeStep.VerifyOld,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PinChangeEvent {
    data object PinChanged : PinChangeEvent
}

@HiltViewModel
class PinChangeViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PinChangeState())
    val state: StateFlow<PinChangeState> = _state.asStateFlow()

    private val _events = Channel<PinChangeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Holds the new PIN between EnterNew and ConfirmNew steps.
    private var pendingNewPin: String = ""

    fun onDigit(digit: Char) {
        val s = _state.value
        if (s.isLoading || s.digits.length >= 4) return
        val next = s.digits + digit
        _state.update { it.copy(digits = next, errorMessage = null) }
        if (next.length == 4) onFourDigitsEntered(next)
    }

    fun onDelete() {
        _state.update { it.copy(digits = it.digits.dropLast(1), errorMessage = null) }
    }

    private fun onFourDigitsEntered(digits: String) {
        when (_state.value.step) {
            PinChangeStep.VerifyOld -> verifyOldPin(digits)
            PinChangeStep.EnterNew -> {
                pendingNewPin = digits
                _state.update { it.copy(digits = "", step = PinChangeStep.ConfirmNew) }
            }
            PinChangeStep.ConfirmNew -> confirmNewPin(digits)
        }
    }

    private fun verifyOldPin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val ok = runCatching { userRepository.verifyPin(pin) }.getOrDefault(false)
            if (ok) {
                _state.update { it.copy(isLoading = false, digits = "", step = PinChangeStep.EnterNew) }
            } else {
                _state.update { it.copy(isLoading = false, digits = "", errorMessage = "Incorrect PIN") }
            }
        }
    }

    private fun confirmNewPin(confirm: String) {
        if (confirm != pendingNewPin) {
            pendingNewPin = ""
            _state.update {
                it.copy(
                    digits = "",
                    step = PinChangeStep.EnterNew,
                    errorMessage = "PINs don't match — try again",
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { userRepository.createPin(pendingNewPin) }
            pendingNewPin = ""
            _state.update { it.copy(isLoading = false, digits = "") }
            _events.send(PinChangeEvent.PinChanged)
        }
    }
}
