package com.nightcatchers.feature.parental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareStatus
import com.nightcatchers.core.domain.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentalDashboardViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
) : ViewModel() {

    data class UiState(val pendingShares: List<PendingShare> = emptyList())

    val uiState: StateFlow<UiState> = shareRepository.observePendingShares()
        .map { UiState(pendingShares = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    fun approveShare(shareId: String) {
        viewModelScope.launch {
            shareRepository.updateStatus(shareId, ShareStatus.APPROVED)
        }
    }

    fun declineShare(shareId: String) {
        viewModelScope.launch {
            shareRepository.updateStatus(shareId, ShareStatus.DECLINED)
        }
    }
}
