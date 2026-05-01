package com.nightcatchers.feature.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.common.DeviceTier
import com.nightcatchers.core.common.DeviceTierDetector
import com.nightcatchers.core.domain.model.MonsterArchetype
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(
    private val spawnEngine: MonsterSpawnEngine,
    private val tierDetector: DeviceTierDetector,
) : ViewModel() {

    val deviceTier: DeviceTier by lazy { tierDetector.detect().tier }

    val uiState: StateFlow<ArUiState> = spawnEngine.spawnEvents
        .map { event ->
            when (event) {
                is MonsterSpawnEvent.MonsterDetected -> ArUiState.MonsterSpawned(
                    archetype = event.archetype,
                    screenX = event.screenX,
                    screenY = event.screenY,
                )
                MonsterSpawnEvent.NoMonsterVisible -> ArUiState.Scanning
                MonsterSpawnEvent.Scanning -> ArUiState.Scanning
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArUiState.Scanning,
        )

    fun onFrameAvailable(timestampMs: Long, hasValidPlane: Boolean) {
        spawnEngine.onFrameAvailable(timestampMs, hasValidPlane)
    }
}

sealed interface ArUiState {
    data object Scanning : ArUiState
    data class MonsterSpawned(
        val archetype: MonsterArchetype,
        val screenX: Float,
        val screenY: Float,
    ) : ArUiState
}
