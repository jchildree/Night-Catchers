package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetStats
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetMoodStateUseCase @Inject constructor() {

    operator fun invoke(stats: PetStats, lastInteractedAt: Instant = Instant.now()): Mood =
        deriveMood(stats, lastInteractedAt)

    private fun deriveMood(stats: PetStats, lastInteractedAt: Instant): Mood = when {
        ChronoUnit.DAYS.between(lastInteractedAt, Instant.now()) >= 7 -> Mood.MISSING_YOU
        stats.energy < 20 -> Mood.SLEEPY
        stats.hunger < 20 -> Mood.GRUMPY
        stats.happiness < 20 -> Mood.LONELY
        stats.spookiness > 85 -> Mood.SPOOKED
        stats.trust >= 80 -> Mood.BONDED
        stats.hunger > 80 && stats.happiness > 80 && stats.energy > 80 -> Mood.ECSTATIC
        stats.happiness > 80 && stats.energy > 70 -> Mood.EXCITED
        stats.happiness > 60 && stats.trust > 50 -> Mood.PLAYFUL
        else -> Mood.CONTENT
    }
}
