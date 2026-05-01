package com.nightcatchers.core.domain.model

/**
 * Time-of-day phase that governs available mini-games and stat bonuses (section 18).
 */
enum class DayPhase(val hourStart: Int, val hourEnd: Int) {
    MORNING(6, 11),
    AFTERNOON(12, 17),
    EVENING(18, 20),
    NIGHT(21, 5),      // wraps midnight
    ;

    companion object {
        fun fromHour(hour: Int): DayPhase = entries.firstOrNull { phase ->
            if (phase == NIGHT) hour >= 21 || hour <= 5
            else hour in phase.hourStart..phase.hourEnd
        } ?: AFTERNOON
    }
}
