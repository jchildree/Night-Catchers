package com.nightcatchers.core.domain.model

import java.time.LocalTime

data class SafetyPolicy(
    val tier: AccountTier,
    val isBedtime: Boolean,
    val sessionMinutesUsed: Int,
) {
    fun evaluate(): PolicyResult {
        if (isBedtime && tier != AccountTier.ADULT) return PolicyResult.HARD_BLOCK
        val cap = tier.dailyCapMinutes ?: return PolicyResult.ALLOW
        return when {
            sessionMinutesUsed >= cap -> PolicyResult.HARD_BLOCK
            sessionMinutesUsed >= cap - 5 -> PolicyResult.SOFT_BLOCK
            else -> PolicyResult.ALLOW
        }
    }

    companion object {
        fun isBedtime(tier: AccountTier, hour: Int): Boolean {
            if (tier == AccountTier.ADULT) return false
            return hour >= tier.bedtimeStart || hour < tier.bedtimeEnd
        }
    }
}
