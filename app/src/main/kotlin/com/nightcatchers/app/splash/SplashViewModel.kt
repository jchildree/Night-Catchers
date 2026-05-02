package com.nightcatchers.app.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Pending : SplashDestination
    data object Onboarding : SplashDestination
    data object Home : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Pending)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = runCatching { userRepository.getUserProfile() }.getOrNull()
            _destination.value = if (profile?.childFirstName?.isNotBlank() == true) {
                SplashDestination.Home
            } else {
                SplashDestination.Onboarding
            }
        }
    }
}
