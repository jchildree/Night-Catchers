package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.usecase.GetMoodStateUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetMoodStateUseCaseTest {

    private val useCase = GetMoodStateUseCase()

    @Test
    fun `returns SLEEPY when energy is below 20`() {
        val stats = PetStats(hunger = 80, happiness = 80, energy = 15, spookiness = 50, trust = 30)
        useCase(stats) shouldBe Mood.SLEEPY
    }

    @Test
    fun `returns GRUMPY when hunger is below 20`() {
        val stats = PetStats(hunger = 10, happiness = 80, energy = 80, spookiness = 50, trust = 30)
        useCase(stats) shouldBe Mood.GRUMPY
    }

    @Test
    fun `returns LONELY when happiness is below 20`() {
        val stats = PetStats(hunger = 80, happiness = 10, energy = 80, spookiness = 50, trust = 30)
        useCase(stats) shouldBe Mood.LONELY
    }

    @Test
    fun `returns SPOOKED when spookiness is above 85`() {
        val stats = PetStats(hunger = 80, happiness = 80, energy = 80, spookiness = 90, trust = 30)
        useCase(stats) shouldBe Mood.SPOOKED
    }

    @Test
    fun `returns EXCITED when happiness and energy are both high`() {
        val stats = PetStats(hunger = 80, happiness = 85, energy = 80, spookiness = 50, trust = 30)
        useCase(stats) shouldBe Mood.EXCITED
    }

    @Test
    fun `returns PLAYFUL when happiness above 60 and trust above 50`() {
        val stats = PetStats(hunger = 80, happiness = 70, energy = 65, spookiness = 50, trust = 60)
        useCase(stats) shouldBe Mood.PLAYFUL
    }

    @Test
    fun `returns CONTENT for balanced stats`() {
        val stats = PetStats(hunger = 80, happiness = 60, energy = 60, spookiness = 50, trust = 20)
        useCase(stats) shouldBe Mood.CONTENT
    }

    // ── MISSING_YOU ──────────────────────────────────────────────────────────

    @Test
    fun `returns MISSING_YOU when last interaction was 8 days ago`() {
        val stats = PetStats(hunger = 80, happiness = 80, energy = 80, spookiness = 50, trust = 30)
        val eightDaysAgo = Instant.now().minus(8, ChronoUnit.DAYS)
        useCase(stats, eightDaysAgo) shouldBe Mood.MISSING_YOU
    }

    @Test
    fun `returns MISSING_YOU even when stats are critical — inactivity is highest priority`() {
        val stats = PetStats(hunger = 5, happiness = 5, energy = 5, spookiness = 90, trust = 0)
        val tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS)
        useCase(stats, tenDaysAgo) shouldBe Mood.MISSING_YOU
    }

    @Test
    fun `does NOT return MISSING_YOU when inactive for only 6 days`() {
        val stats = PetStats(hunger = 80, happiness = 80, energy = 15, spookiness = 50, trust = 30)
        val sixDaysAgo = Instant.now().minus(6, ChronoUnit.DAYS)
        useCase(stats, sixDaysAgo) shouldBe Mood.SLEEPY
    }

    // ── BONDED ────────────────────────────────────────────────────────────────

    @Test
    fun `returns BONDED when trust reaches 80`() {
        val stats = PetStats(hunger = 80, happiness = 70, energy = 70, spookiness = 50, trust = 80)
        useCase(stats) shouldBe Mood.BONDED
    }

    @Test
    fun `negative stat outranks BONDED — GRUMPY when hunger low despite high trust`() {
        val stats = PetStats(hunger = 10, happiness = 70, energy = 70, spookiness = 50, trust = 80)
        useCase(stats) shouldBe Mood.GRUMPY
    }

    @Test
    fun `does NOT return BONDED when trust is 79`() {
        val stats = PetStats(hunger = 80, happiness = 70, energy = 70, spookiness = 50, trust = 79)
        useCase(stats) shouldBe Mood.PLAYFUL
    }

    // ── ECSTATIC ──────────────────────────────────────────────────────────────

    @Test
    fun `returns ECSTATIC when hunger happiness and energy are all above 80`() {
        val stats = PetStats(hunger = 85, happiness = 85, energy = 85, spookiness = 50, trust = 50)
        useCase(stats) shouldBe Mood.ECSTATIC
    }

    @Test
    fun `does NOT return ECSTATIC when trust is 80 — BONDED takes priority`() {
        val stats = PetStats(hunger = 85, happiness = 85, energy = 85, spookiness = 50, trust = 80)
        useCase(stats) shouldBe Mood.BONDED
    }

    @Test
    fun `does NOT return ECSTATIC when only happiness and energy are high but hunger is not`() {
        val stats = PetStats(hunger = 70, happiness = 85, energy = 80, spookiness = 50, trust = 30)
        useCase(stats) shouldBe Mood.EXCITED
    }
}
