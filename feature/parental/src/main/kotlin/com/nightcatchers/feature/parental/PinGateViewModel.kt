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

data class PinGateState(
    val digits: String = "",
    val confirmDigits: String = "",
    val isVerifying: Boolean = false,
    val errorMessage: String? = null,
    // true = verify existing PIN; false = create new PIN (two-step: enter then confirm)
    val hasPin: Boolean = true,
    val awaitingConfirm: Boolean = false,
)

sealed interface PinGateEvent {
    data object PinVerified : PinGateEvent
}

@HiltViewModel
class PinGateViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PinGateState())
    val state: StateFlow<PinGateState> = _state.asStateFlow()

    private val _events = Channel<PinGateEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            _state.update { it.copy(hasPin = profile?.pinHash?.isNotBlank() == true) }
        }
    }

    fun onDigit(digit: Char) {
        val s = _state.value
        if (s.hasPin) {
            if (s.digits.length >= 4) return
            val next = s.digits + digit
            _state.update { it.copy(digits = next, errorMessage = null) }
            if (next.length == 4) verifyPin(next)
        } else if (!s.awaitingConfirm) {
            if (s.digits.length >= 4) return
            val next = s.digits + digit
            _state.update { it.copy(digits = next, errorMessage = null) }
            if (next.length == 4) _state.update { it.copy(awaitingConfirm = true) }
        } else {
            if (s.confirmDigits.length >= 4) return
            val next = s.confirmDigits + digit
            _state.update { it.copy(confirmDigits = next, errorMessage = null) }
            if (next.length == 4) confirmCreate(s.digits, next)
        }
    }

    fun onDelete() {
        val s = _state.value
        if (s.awaitingConfirm) {
            _state.update { it.copy(confirmDigits = it.confirmDigits.dropLast(1), errorMessage = null) }
        } else {
            _state.update { it.copy(digits = it.digits.dropLast(1), errorMessage = null) }
        }
    }

    fun onCancelConfirm() {
        _state.update { it.copy(awaitingConfirm = false, digits = "", confirmDigits = "", errorMessage = null) }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true) }
            val ok = runCatching { userRepository.verifyPin(pin) }.getOrDefault(false)
            if (ok) {
                _state.update { it.copy(isVerifying = false, digits = "") }
                _events.send(PinGateEvent.PinVerified)
            } else {
                _state.update { it.copy(isVerifying = false, digits = "", errorMessage = "Incorrect PIN") }
            }
        }
    }

    private fun confirmCreate(newPin: String, confirm: String) {
        if (newPin != confirm) {
            _state.update {
                it.copy(
                    awaitingConfirm = false,
                    digits = "",
                    confirmDigits = "",
                    errorMessage = "PINs don't match — try again",
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true) }
            runCatching { userRepository.createPin(newPin) }
            _state.update {
                it.copy(isVerifying = false, hasPin = true, digits = "", confirmDigits = "")
            }
            _events.send(PinGateEvent.PinVerified)
        }
    }
}
