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
    val isVerifying: Boolean = false,
    val errorMessage: String? = null,
    val hasPin: Boolean = true,
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
        val current = _state.value.digits
        if (current.length >= 4) return
        val next = current + digit
        _state.update { it.copy(digits = next, errorMessage = null) }
        if (next.length == 4) verify(next)
    }

    fun onDelete() {
        _state.update { it.copy(digits = it.digits.dropLast(1), errorMessage = null) }
    }

    private fun verify(pin: String) {
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
}
