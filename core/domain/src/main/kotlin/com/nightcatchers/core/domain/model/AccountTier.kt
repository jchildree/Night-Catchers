package com.nightcatchers.core.domain.model

/**
 * Account tier governs content access, session caps, and COPPA/GDPR-K compliance.
 */
enum class AccountTier(
    val dailyCapMinutes: Int?,     // null = no cap
    val sessionCapHours: Int?,
    val bedtimeStart: Int,          // 24h hour
    val bedtimeEnd: Int,
) {
    CHILD(dailyCapMinutes = 30, sessionCapHours = 2, bedtimeStart = 20, bedtimeEnd = 7),
    TEEN(dailyCapMinutes = 60, sessionCapHours = 4, bedtimeStart = 22, bedtimeEnd = 7),
    ADULT(dailyCapMinutes = null, sessionCapHours = null, bedtimeStart = -1, bedtimeEnd = -1),
}

enum class PolicyResult { ALLOW, SOFT_BLOCK, HARD_BLOCK }
