package com.nightcatchers.core.domain.minigames

import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.usecase.minigames.GetFeaturedGameUseCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetFeaturedGameUseCaseTest {

    private val useCase = GetFeaturedGameUseCase()

    @Test
    fun `returns game at index dayOfYear mod 6`() {
        val date = LocalDate.of(2026, 1, 1) // dayOfYear = 1, 1 % 6 = 1
        val expected = MiniGameId.entries[1 % MiniGameId.entries.size]
        useCase(date) shouldBe expected
    }

    @Test
    fun `returns game at index 0 when dayOfYear is multiple of 6`() {
        // dayOfYear = 6 → 6 % 6 = 0
        val date = LocalDate.of(2026, 1, 6)
        val expected = MiniGameId.entries[0]
        useCase(date) shouldBe expected
    }

    @Test
    fun `returns game at index 5 when dayOfYear mod 6 equals 5`() {
        // dayOfYear = 5 → 5 % 6 = 5
        val date = LocalDate.of(2026, 1, 5)
        val expected = MiniGameId.entries[5]
        useCase(date) shouldBe expected
    }

    @Test
    fun `all 6 games are covered across 6 consecutive days`() {
        val startDate = LocalDate.of(2026, 3, 1)
        val results = (0 until 6).map { offset -> useCase(startDate.plusDays(offset.toLong())) }.toSet()
        results.size shouldBe 6
    }

    @Test
    fun `consecutive days produce different games`() {
        val date = LocalDate.of(2026, 5, 9)
        val today = useCase(date)
        val tomorrow = useCase(date.plusDays(1))
        today shouldNotBe tomorrow
    }

    @Test
    fun `same date always returns same game`() {
        val date = LocalDate.of(2026, 7, 15)
        useCase(date) shouldBe useCase(date)
    }

    @Test
    fun `returns valid MiniGameId for any date in year`() {
        val baseDate = LocalDate.of(2026, 1, 1)
        (0 until 365).forEach { offset ->
            val result = useCase(baseDate.plusDays(offset.toLong()))
            MiniGameId.entries.contains(result) shouldBe true
        }
    }
}
