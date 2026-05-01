package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.usecase.GetMoodStateUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
}
