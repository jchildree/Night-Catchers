package com.nightcatchers.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.UserProfile
import com.nightcatchers.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OnboardingState(
    val page: Int = 0,
    val childName: String = "",
    val cameraPermissionGranted: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface OnboardingEvent {
    data object NavigateToHome : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(childName = name.take(20)) }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(cameraPermissionGranted = granted) }
    }

    fun onNextPage() {
        _state.update { it.copy(page = (it.page + 1).coerceAtMost(2)) }
    }

    fun onComplete() {
        val name = _state.value.childName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            runCatching {
                userRepository.saveUserProfile(
                    UserProfile(
                        parentUid = "",          // populated when parent sets up account
                        childId = UUID.randomUUID().toString(),
                        childFirstName = name,
                        pinHash = "",            // set when parent creates PIN
                        createdAt = System.currentTimeMillis(),
                    ),
                )
            }
            _state.update { it.copy(isSaving = false) }
            _events.send(OnboardingEvent.NavigateToHome)
        }
    }
}
