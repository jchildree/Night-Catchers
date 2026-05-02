package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.RoomStage
import com.nightcatchers.core.domain.usecase.GetRoomStageUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GetRoomStageUseCaseTest {

    private val useCase = GetRoomStageUseCase()

    @Test
    fun `returns HOLDING_PEN at trust 0`() {
        useCase(0) shouldBe RoomStage.HOLDING_PEN
    }

    @Test
    fun `returns HOLDING_PEN at trust 19`() {
        useCase(19) shouldBe RoomStage.HOLDING_PEN
    }

    @Test
    fun `returns COSY_CORNER at trust 20`() {
        useCase(20) shouldBe RoomStage.COSY_CORNER
    }

    @Test
    fun `returns BEDROOM at trust 40`() {
        useCase(40) shouldBe RoomStage.BEDROOM
    }

    @Test
    fun `returns SANCTUARY at trust 60`() {
        useCase(60) shouldBe RoomStage.SANCTUARY
    }

    @Test
    fun `returns DREAM_ROOM at trust 80`() {
        useCase(80) shouldBe RoomStage.DREAM_ROOM
    }

    @Test
    fun `returns DREAM_ROOM at trust 100`() {
        useCase(100) shouldBe RoomStage.DREAM_ROOM
    }
}
