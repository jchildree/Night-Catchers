package com.nightcatchers.core.domain.minigames

import com.nightcatchers.core.domain.model.minigames.BlobColor
import com.nightcatchers.core.domain.model.minigames.BlobEntity
import com.nightcatchers.core.domain.model.minigames.BlobShape
import com.nightcatchers.core.domain.model.minigames.JarEntity
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.minigames.MatchResult
import com.nightcatchers.core.domain.usecase.minigames.SlimeSortUseCase
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SlimeSortUseCaseTest {

    private val petRepository: PetRepository = mockk()
    private val useCase = SlimeSortUseCase(petRepository)

    // ── Helper builders ─────────────────────────────────────────────────────

    private fun blob(
        color: BlobColor = BlobColor.RED,
        shape: BlobShape = BlobShape.CIRCLE,
        isPhantom: Boolean = false,
    ) = BlobEntity(id = "blob-1", color = color, shape = shape, isPhantom = isPhantom)

    private fun jar(
        color: BlobColor = BlobColor.RED,
        shape: BlobShape = BlobShape.CIRCLE,
    ) = JarEntity(id = "jar-1", acceptsColor = color, acceptsShape = shape, label = "${color.emoji}${shape.label}")

    // ── checkMatch ──────────────────────────────────────────────────────────

    @Test
    fun `checkMatch returns Hit when both color and shape match`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.RED, shape = BlobShape.CIRCLE),
            jar = jar(color = BlobColor.RED, shape = BlobShape.CIRCLE),
        )
        result shouldBe MatchResult.Hit
    }

    @Test
    fun `checkMatch returns WrongColor when only shape matches`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.BLUE, shape = BlobShape.CIRCLE),
            jar = jar(color = BlobColor.RED, shape = BlobShape.CIRCLE),
        )
        result shouldBe MatchResult.WrongColor
    }

    @Test
    fun `checkMatch returns WrongShape when only color matches`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.RED, shape = BlobShape.STAR),
            jar = jar(color = BlobColor.RED, shape = BlobShape.CIRCLE),
        )
        result shouldBe MatchResult.WrongShape
    }

    @Test
    fun `checkMatch returns WrongBoth when neither color nor shape matches`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.BLUE, shape = BlobShape.STAR),
            jar = jar(color = BlobColor.RED, shape = BlobShape.CIRCLE),
        )
        result shouldBe MatchResult.WrongBoth
    }

    @Test
    fun `checkMatch returns Phantom for phantom blobs regardless of jar`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.RED, shape = BlobShape.CIRCLE, isPhantom = true),
            jar = jar(color = BlobColor.RED, shape = BlobShape.CIRCLE),
        )
        result shouldBe MatchResult.Phantom
    }

    @Test
    fun `checkMatch returns Phantom for phantom blob even when colors and shapes differ`() {
        val result = useCase.checkMatch(
            blob = blob(color = BlobColor.GREEN, shape = BlobShape.HEXAGON, isPhantom = true),
            jar = jar(color = BlobColor.YELLOW, shape = BlobShape.TRIANGLE),
        )
        result shouldBe MatchResult.Phantom
    }

    // ── generateJars ────────────────────────────────────────────────────────

    @Test
    fun `generateJars(1) returns 2 jars`() {
        val jars = useCase.generateJars(1)
        jars shouldHaveSize 2
    }

    @Test
    fun `generateJars(1) has 2 unique colors`() {
        val jars = useCase.generateJars(1)
        jars.map { it.acceptsColor }.toSet().size shouldBe 2
    }

    @Test
    fun `generateJars(1) has 2 unique shapes`() {
        val jars = useCase.generateJars(1)
        jars.map { it.acceptsShape }.toSet().size shouldBe 2
    }

    @Test
    fun `generateJars(2) returns 3 jars`() {
        val jars = useCase.generateJars(2)
        jars shouldHaveSize 3
    }

    @Test
    fun `generateJars(3) returns 4 jars`() {
        val jars = useCase.generateJars(3)
        jars shouldHaveSize 4
    }

    @Test
    fun `generateJars(3) has 4 unique colors`() {
        val jars = useCase.generateJars(3)
        jars.map { it.acceptsColor }.toSet().size shouldBe 4
    }

    @Test
    fun `generateJars round above 3 still returns 4 jars`() {
        val jars = useCase.generateJars(5)
        jars shouldHaveSize 4
    }
}
