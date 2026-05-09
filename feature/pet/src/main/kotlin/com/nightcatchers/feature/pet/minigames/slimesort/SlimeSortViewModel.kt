package com.nightcatchers.feature.pet.minigames.slimesort

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.minigames.BlobEntity
import com.nightcatchers.core.domain.model.minigames.JarEntity
import com.nightcatchers.core.domain.usecase.minigames.MatchResult
import com.nightcatchers.core.domain.usecase.minigames.SlimeSortUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ──────────────────────────────────────────────────────────────────

sealed interface SlimeSortUiState {
    data object Loading : SlimeSortUiState
    data class Playing(
        val round: Int,
        val blobs: List<ActiveBlob>,
        val jars: List<JarEntity>,
        val combo: Int,
        val score: Int,
        val missesThisRound: Int,
        val draggedBlobId: String?,
        val dragOffset: Offset,
    ) : SlimeSortUiState
    data class Complete(val score: Int, val maxCombo: Int) : SlimeSortUiState
}

data class ActiveBlob(
    val entity: BlobEntity,
    val position: Offset,
    val isBeingDragged: Boolean,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface SlimeSortEvent {
    data class BlobMatched(val blobId: String, val jarId: String) : SlimeSortEvent
    data class BlobSplatted(val blobId: String, val isPhantom: Boolean) : SlimeSortEvent
    data class ComboAchieved(val combo: Int) : SlimeSortEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

private const val MAX_ROUNDS = 3
private const val MAX_MISSES_PER_ROUND = 3
private const val COMBO_THRESHOLD = 3
private const val SCORE_PER_HIT = 10
private const val SCORE_PER_COMBO_BONUS = 10

@HiltViewModel
class SlimeSortViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val slimeSortUseCase: SlimeSortUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _uiState = MutableStateFlow<SlimeSortUiState>(SlimeSortUiState.Loading)
    val uiState: StateFlow<SlimeSortUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SlimeSortEvent>()
    val events: SharedFlow<SlimeSortEvent> = _events.asSharedFlow()

    /** Tracks highest combo seen across the session. */
    private var maxCombo = 0

    /** Consecutive correct matches. */
    private var consecutiveHits = 0

    private var spawnJob: Job? = null

    init {
        startRound(round = 1)
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun onBlobDragStart(blobId: String, offset: Offset) {
        val playing = _uiState.value as? SlimeSortUiState.Playing ?: return
        _uiState.update {
            playing.copy(
                blobs = playing.blobs.map { b ->
                    if (b.entity.id == blobId) b.copy(isBeingDragged = true) else b
                },
                draggedBlobId = blobId,
                dragOffset = offset,
            )
        }
    }

    fun onBlobDragUpdate(offset: Offset) {
        val playing = _uiState.value as? SlimeSortUiState.Playing ?: return
        _uiState.update { playing.copy(dragOffset = offset) }
    }

    /**
     * Called when the user releases a blob.
     * @param jarId the id of the jar under the blob, or null if released over empty space.
     */
    fun onBlobReleased(jarId: String?) {
        val playing = _uiState.value as? SlimeSortUiState.Playing ?: return
        val blobId = playing.draggedBlobId ?: return
        val blob = playing.blobs.firstOrNull { it.entity.id == blobId }?.entity ?: return

        viewModelScope.launch {
            if (jarId == null) {
                // Released over no jar — splat
                handleMiss(playing, blobId, isPhantom = blob.isPhantom)
                return@launch
            }
            val jar = playing.jars.firstOrNull { it.id == jarId } ?: run {
                handleMiss(playing, blobId, isPhantom = blob.isPhantom)
                return@launch
            }
            when (slimeSortUseCase.checkMatch(blob, jar)) {
                MatchResult.Hit -> handleHit(playing, blobId, jarId)
                MatchResult.Phantom -> handlePhantomSplat(playing, blobId)
                else -> handleMiss(playing, blobId, isPhantom = false)
            }
        }
    }

    // ── Internal logic ────────────────────────────────────────────────────────

    private fun startRound(round: Int) {
        val jars = slimeSortUseCase.generateJars(round)
        consecutiveHits = 0
        _uiState.update {
            SlimeSortUiState.Playing(
                round = round,
                blobs = emptyList(),
                jars = jars,
                combo = 0,
                score = currentScore(),
                missesThisRound = 0,
                draggedBlobId = null,
                dragOffset = Offset.Zero,
            )
        }
        spawnJob?.cancel()
        spawnJob = viewModelScope.launch { spawnLoop(round) }
    }

    private suspend fun spawnLoop(round: Int) {
        val gapMs = when {
            round >= 3 -> 1_200L
            round == 2 -> 1_800L
            else -> 2_500L
        }
        while (true) {
            val playing = _uiState.value as? SlimeSortUiState.Playing ?: break
            if (playing.round != round) break
            val isPhantom = round >= 3 && Math.random() < 0.2
            val newBlob = ActiveBlob(
                entity = slimeSortUseCase.generateBlob(round, isPhantom = isPhantom),
                position = Offset(x = (100..900).random().toFloat(), y = 0f),
                isBeingDragged = false,
            )
            _uiState.update { s ->
                (s as? SlimeSortUiState.Playing)?.copy(blobs = s.blobs + newBlob) ?: s
            }
            delay(gapMs)
        }
    }

    private suspend fun handleHit(playing: SlimeSortUiState.Playing, blobId: String, jarId: String) {
        consecutiveHits++
        val combo = if (consecutiveHits >= COMBO_THRESHOLD) consecutiveHits - COMBO_THRESHOLD + 2 else 1
        if (combo > maxCombo) maxCombo = combo
        val bonusPoints = if (combo > 1) SCORE_PER_COMBO_BONUS else 0
        val newScore = playing.score + SCORE_PER_HIT * combo + bonusPoints

        _uiState.update { s ->
            (s as? SlimeSortUiState.Playing)?.copy(
                blobs = s.blobs.filterNot { it.entity.id == blobId },
                combo = combo,
                score = newScore,
                draggedBlobId = null,
                dragOffset = Offset.Zero,
            ) ?: s
        }
        _events.emit(SlimeSortEvent.BlobMatched(blobId, jarId))
        if (combo >= COMBO_THRESHOLD) {
            _events.emit(SlimeSortEvent.ComboAchieved(combo))
        }
        checkRoundProgress()
    }

    private suspend fun handleMiss(playing: SlimeSortUiState.Playing, blobId: String, isPhantom: Boolean) {
        consecutiveHits = 0
        val newMisses = playing.missesThisRound + if (isPhantom) 0 else 1

        _uiState.update { s ->
            (s as? SlimeSortUiState.Playing)?.copy(
                blobs = s.blobs.filterNot { it.entity.id == blobId },
                combo = 0,
                missesThisRound = newMisses,
                draggedBlobId = null,
                dragOffset = Offset.Zero,
            ) ?: s
        }
        _events.emit(SlimeSortEvent.BlobSplatted(blobId, isPhantom))
        checkRoundProgress()
    }

    private suspend fun handlePhantomSplat(playing: SlimeSortUiState.Playing, blobId: String) {
        // Phantom splats are not punishing — no miss counted
        _uiState.update { s ->
            (s as? SlimeSortUiState.Playing)?.copy(
                blobs = s.blobs.filterNot { it.entity.id == blobId },
                draggedBlobId = null,
                dragOffset = Offset.Zero,
            ) ?: s
        }
        _events.emit(SlimeSortEvent.BlobSplatted(blobId, isPhantom = true))
    }

    private fun checkRoundProgress() {
        val playing = _uiState.value as? SlimeSortUiState.Playing ?: return
        if (playing.missesThisRound >= MAX_MISSES_PER_ROUND) {
            advanceOrComplete(playing)
        }
    }

    private fun advanceOrComplete(playing: SlimeSortUiState.Playing) {
        spawnJob?.cancel()
        val nextRound = playing.round + 1
        if (nextRound > MAX_ROUNDS) {
            completeSession(playing.score)
        } else {
            startRound(nextRound)
        }
    }

    private fun completeSession(finalScore: Int) {
        _uiState.update { SlimeSortUiState.Complete(score = finalScore, maxCombo = maxCombo) }
        viewModelScope.launch {
            runCatching { slimeSortUseCase.applyResult(monsterId, finalScore, maxCombo) }
        }
    }

    private fun currentScore(): Int =
        (_uiState.value as? SlimeSortUiState.Playing)?.score ?: 0

    override fun onCleared() {
        super.onCleared()
        spawnJob?.cancel()
    }
}
