package com.nightcatchers.core.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DeviceTierDetectorTest {

    @Test
    fun `6000 MB RAM + ARCore + GL ES 3_1 classifies as Tier A`() {
        DeviceTierDetector.classifyTier(ram = 6_000, hasArCore = true, glVersion = 3.1f) shouldBe DeviceTier.A
    }

    @Test
    fun `6000 MB RAM + ARCore + GL ES 3_0 classifies as Tier B not A`() {
        DeviceTierDetector.classifyTier(ram = 6_000, hasArCore = true, glVersion = 3.0f) shouldBe DeviceTier.B
    }

    @Test
    fun `4000 MB RAM + no ARCore + GL ES 3_0 classifies as Tier B`() {
        DeviceTierDetector.classifyTier(ram = 4_000, hasArCore = false, glVersion = 3.0f) shouldBe DeviceTier.B
    }

    @Test
    fun `4000 MB RAM + ARCore + GL ES 2_0 classifies as Tier C`() {
        DeviceTierDetector.classifyTier(ram = 4_000, hasArCore = true, glVersion = 2.0f) shouldBe DeviceTier.C
    }

    @Test
    fun `2000 MB RAM + GL ES 3_0 classifies as Tier C`() {
        DeviceTierDetector.classifyTier(ram = 2_000, hasArCore = false, glVersion = 3.0f) shouldBe DeviceTier.C
    }

    @Test
    fun `8000 MB RAM + ARCore + GL ES 3_1 classifies as Tier A`() {
        DeviceTierDetector.classifyTier(ram = 8_000, hasArCore = true, glVersion = 3.1f) shouldBe DeviceTier.A
    }
}
