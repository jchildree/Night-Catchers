package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.usecase.GetFeaturedMiniGameUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetFeaturedMiniGameUseCaseTest {

    private val useCase = GetFeaturedMiniGameUseCase()

    @Test
    fun `dayOfYear 1 yields first game in catalog`() {
        // Jan 1 → dayOfYear 1 → (1-1) % 6 = 0 → first game
        val date = LocalDate.of(2026, 1, 1)
        useCase(date) shouldBe MiniGameId.entries.first()
    }

    @Test
    fun `rotation cycles through all six games`() {
        val seen = (0..5).map { dayOffset ->
            useCase(LocalDate.of(2026, 1, 1).plusDays(dayOffset.toLong()))
        }.toSet()
        seen.size shouldBe 6
    }

    @Test
    fun `rotation is deterministic for same date`() {
        val date = LocalDate.of(2026, 6, 15)
        useCase(date) shouldBe useCase(date)
    }
}
