package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.BondStage
import com.nightcatchers.core.domain.usecase.GetBondStageUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GetBondStageUseCaseTest {

    private val useCase = GetBondStageUseCase()

    @Test
    fun `returns STRANGER at trust 0`() {
        useCase(0) shouldBe BondStage.STRANGER
    }

    @Test
    fun `returns STRANGER below threshold 20`() {
        useCase(19) shouldBe BondStage.STRANGER
    }

    @Test
    fun `returns CURIOUS at trust 20`() {
        useCase(20) shouldBe BondStage.CURIOUS
    }

    @Test
    fun `returns FRIENDLY at trust 40`() {
        useCase(40) shouldBe BondStage.FRIENDLY
    }

    @Test
    fun `returns BONDED at trust 60`() {
        useCase(60) shouldBe BondStage.BONDED
    }

    @Test
    fun `returns BEST_FRIENDS at trust 80`() {
        useCase(80) shouldBe BondStage.BEST_FRIENDS
    }

    @Test
    fun `returns BEST_FRIENDS at trust 100`() {
        useCase(100) shouldBe BondStage.BEST_FRIENDS
    }
}
