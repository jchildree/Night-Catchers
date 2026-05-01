package com.nightcatchers.feature.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.usecase.CaptureMonsterUseCase
import com.nightcatchers.feature.ar.ArUiState
import com.nightcatchers.feature.ar.MonsterSpawnEngine
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
    private val spawnEngine: MonsterSpawnEngine,
    private val captureMonsterUseCase: CaptureMonsterUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    private var holdJob: Job? = null

    init {
        observeSpawnEvents()
    }

    private fun observeSpawnEvents() {
        viewModelScope.launch {
            spawnEngine.spawnEvents.collect { event ->
                when (event) {
                    is com.nightcatchers.feature.ar.MonsterSpawnEvent.MonsterDetected -> {
                        if (_state.value is CaptureState.Idle || _state.value is CaptureState.Scanning) {
                            _state.value = CaptureState.MonsterSpawned(
                                archetype = event.archetype,
                                anchorX = event.screenX,
                                anchorY = event.screenY,
                            )
                        }
                    }
                    com.nightcatchers.feature.ar.MonsterSpawnEvent.Scanning -> {
                        if (_state.value is CaptureState.Idle) {
                            _state.value = CaptureState.Scanning
                        }
                    }
                    com.nightcatchers.feature.ar.MonsterSpawnEvent.NoMonsterVisible -> {
                        if (_state.value is CaptureState.Scanning) {
                            _state.value = CaptureState.Idle
                        }
                    }
                }
            }
        }
    }

    /** Called when the child presses and holds the beam on the monster. */
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

    /** Called when the child releases the beam before capture completes. */
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
            _state.value = CaptureState.Success(monsterId = monster.id)
        }
    }

    fun reset() {
        holdJob?.cancel()
        _state.value = CaptureState.Idle
    }
}
