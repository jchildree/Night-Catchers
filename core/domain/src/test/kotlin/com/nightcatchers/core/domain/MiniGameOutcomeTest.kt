package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.MiniGameOutcome
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MiniGameOutcomeTest {

    @Test
    fun `score fraction outside 0 to 1 throws`() {
        assertThrows<IllegalArgumentException> {
            MiniGameOutcome(MiniGameId.CUDDLE_STORM, scoreFraction = 1.5f, rawScore = 100)
        }
        assertThrows<IllegalArgumentException> {
            MiniGameOutcome(MiniGameId.CUDDLE_STORM, scoreFraction = -0.1f, rawScore = 100)
        }
    }

    @Test
    fun `perfect score yields full base reward and full energy cost`() {
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.CUDDLE_STORM,
            scoreFraction = 1f,
            rawScore = 60,
        )
        val delta = outcome.statDelta()
        delta.happiness shouldBe 30 // base
        delta.trust shouldBe 8
        delta.spookiness shouldBe -10
        delta.energy shouldBe -15 // full cost
    }

    @Test
    fun `zero score still gives 25 percent floor reward`() {
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.CUDDLE_STORM,
            scoreFraction = 0f,
            rawScore = 0,
        )
        val delta = outcome.statDelta()
        // 30 happiness * 0.25 = 7
        delta.happiness shouldBe 7
        delta.trust shouldBe 2 // 8 * 0.25 = 2
        delta.energy shouldBe -15 // energy cost always full
    }

    @Test
    fun `partial score scales rewards linearly between floor and full`() {
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.GHOST_DASH,
            scoreFraction = 0.5f,
            rawScore = 250,
        )
        val delta = outcome.statDelta()
        // scale = 0.25 + 0.75*0.5 = 0.625
        // happiness 15 * 0.625 = 9
        delta.happiness shouldBe 9
        // spookiness 25 * 0.625 = 15
        delta.spookiness shouldBe 15
        delta.energy shouldBe -20
    }

    @Test
    fun `food toss energy cost matches catalog`() {
        val outcome = MiniGameOutcome(MiniGameId.FOOD_TOSS, scoreFraction = 1f, rawScore = 3)
        outcome.statDelta().energy shouldBe -10
        outcome.statDelta().hunger shouldBe 25
    }
}
