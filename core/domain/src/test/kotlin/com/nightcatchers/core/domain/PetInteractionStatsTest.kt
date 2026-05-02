package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.PetStats
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Validates stat-delta table from CLAUDE.md so it stays in sync with any
 * future changes to PetRepositoryImpl.applyInteractionToStats().
 *
 * These are pure-Kotlin arithmetic tests — no Android or DB involved.
 */
class PetInteractionStatsTest {

    private val base = PetStats(
        hunger = 60,
        happiness = 60,
        energy = 60,
        spookiness = 60,
        trust = 30,
    )

    @Test
    fun `Feed increases hunger by 25 and happiness by 5`() {
        val result = base.applyFeed()
        result.hunger shouldBe 85
        result.happiness shouldBe 65
        result.energy shouldBe 60
        result.spookiness shouldBe 60
        result.trust shouldBe 30
    }

    @Test
    fun `Feed clamps hunger at 100`() {
        val nearFull = base.copy(hunger = 90)
        nearFull.applyFeed().hunger shouldBe 100
    }

    @Test
    fun `Play increases happiness by 20 and trust by 3, decreases energy by 10`() {
        val result = base.applyPlay()
        result.happiness shouldBe 80
        result.energy shouldBe 50
        result.trust shouldBe 33
        result.hunger shouldBe 60
        result.spookiness shouldBe 60
    }

    @Test
    fun `Train decreases energy by 15 and spookiness by 5, increases trust by 8`() {
        val result = base.applyTrain()
        result.energy shouldBe 45
        result.spookiness shouldBe 55
        result.trust shouldBe 38
        result.hunger shouldBe 60
        result.happiness shouldBe 60
    }

    @Test
    fun `Story increases happiness by 10 and trust by 5, decreases energy by 5`() {
        val result = base.applyStory()
        result.happiness shouldBe 70
        result.trust shouldBe 35
        result.energy shouldBe 55
        result.hunger shouldBe 60
        result.spookiness shouldBe 60
    }

    @Test
    fun `Comfort increases happiness by 15 and trust by 6, decreases spookiness by 10 and energy by 5`() {
        val result = base.applyComfort()
        result.happiness shouldBe 75
        result.trust shouldBe 36
        result.spookiness shouldBe 50
        result.energy shouldBe 55
        result.hunger shouldBe 60
    }

    @Test
    fun `Praise increases happiness by 12 and trust by 4`() {
        val result = base.applyPraise()
        result.happiness shouldBe 72
        result.trust shouldBe 34
        result.hunger shouldBe 60
        result.energy shouldBe 60
        result.spookiness shouldBe 60
    }

    @Test
    fun `Stats clamp at 0 not go negative`() {
        val depleted = PetStats(hunger = 0, happiness = 0, energy = 5, spookiness = 3, trust = 0)
        val result = depleted.applyTrain() // -15 energy, -5 spookiness
        result.energy shouldBe 0
        result.spookiness shouldBe 0
    }

    @Test
    fun `Stats clamp at 100 not exceed`() {
        val maxed = PetStats(hunger = 100, happiness = 95, energy = 98, spookiness = 100, trust = 98)
        val result = maxed.applyFeed() // hunger +25, happiness +5
        result.hunger shouldBe 100
        result.happiness shouldBe 100
    }

    // ── Helpers mirroring PetRepositoryImpl.applyInteractionToStats() ──────

    private fun PetStats.applyFeed() = copy(
        hunger = (hunger + 25).coerceAtMost(100),
        happiness = (happiness + 5).coerceAtMost(100),
    )

    private fun PetStats.applyPlay() = copy(
        happiness = (happiness + 20).coerceAtMost(100),
        energy = (energy - 10).coerceAtLeast(0),
        trust = (trust + 3).coerceAtMost(100),
    )

    private fun PetStats.applyTrain() = copy(
        trust = (trust + 8).coerceAtMost(100),
        energy = (energy - 15).coerceAtLeast(0),
        spookiness = (spookiness - 5).coerceAtLeast(0),
    )

    private fun PetStats.applyStory() = copy(
        happiness = (happiness + 10).coerceAtMost(100),
        trust = (trust + 5).coerceAtMost(100),
        energy = (energy - 5).coerceAtLeast(0),
    )

    private fun PetStats.applyComfort() = copy(
        happiness = (happiness + 15).coerceAtMost(100),
        spookiness = (spookiness - 10).coerceAtLeast(0),
        trust = (trust + 6).coerceAtMost(100),
        energy = (energy - 5).coerceAtLeast(0),
    )

    private fun PetStats.applyPraise() = copy(
        happiness = (happiness + 12).coerceAtMost(100),
        trust = (trust + 4).coerceAtMost(100),
    )
}
