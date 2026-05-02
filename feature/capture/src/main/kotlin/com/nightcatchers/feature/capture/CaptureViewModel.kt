package com.nightcatchers.feature.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.common.DeviceTier
import com.nightcatchers.core.common.DeviceTierDetector
import com.nightcatchers.core.domain.model.LensId
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.usecase.CaptureMonsterUseCase
import com.nightcatchers.feature.filters.FilterLayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val captureMonsterUseCase: CaptureMonsterUseCase,
    val filterLayerManager: FilterLayerManager,
    private val tierDetector: DeviceTierDetector,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val deviceTier: DeviceTier by lazy { tierDetector.detect().tier }

    private val archetypeId: String = savedStateHandle.get<String>("archetypeId") ?: ""

    private val _state = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    private var holdJob: Job? = null

    init {
        filterLayerManager.configure(deviceTier)
        seedFromNavArg()
    }

    // Populate the initial state from the archetype ID passed via navigation.
    // This avoids a SharedFlow race where CaptureViewModel subscribes after the
    // MonsterDetected event was already emitted by MonsterSpawnEngine.
    private fun seedFromNavArg() {
        val archetype = MonsterArchetypeCatalog.findById(archetypeId) ?: return
        filterLayerManager.push(LensId.PROTON_PACK)
        _state.value = CaptureState.MonsterSpawned(
            archetype = archetype,
            anchorX = 0.5f,
            anchorY = 0.5f,
        )
    }

    fun onBeamHoldStart(archetype: MonsterArchetype, anchorX: Float, anchorY: Float) {
        holdJob?.cancel()
        holdJob = viewModelScope.launch {
            val holdMs = (archetype.rarity.captureHoldSeconds * 1000).toLong()
            val intervalMs = 50L
            var elapsed = 0L
            while (elapsed < holdMs) {
                _state.value = CaptureState.Capturing(
                    archetype = archetype,
                    anchorX = anchorX,
                    anchorY = anchorY,
                    holdProgress = elapsed.toFloat() / holdMs,
                )
                delay(intervalMs)
                elapsed += intervalMs
            }
            onCaptureComplete(archetype)
        }
    }

    fun onBeamHoldRelease() {
        holdJob?.cancel()
        val current = _state.value
        if (current is CaptureState.Capturing) {
            _state.value = CaptureState.MonsterSpawned(
                archetype = current.archetype,
                anchorX = current.anchorX,
                anchorY = current.anchorY,
            )
        }
    }

    private fun onCaptureComplete(archetype: MonsterArchetype) {
        viewModelScope.launch {
            val monster = captureMonsterUseCase(archetype)
            filterLayerManager.pop()
            _state.value = CaptureState.Success(monsterId = monster.id)
        }
    }

    fun reset() {
        holdJob?.cancel()
        filterLayerManager.pop()
        _state.value = CaptureState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        filterLayerManager.pop()
    }
}
