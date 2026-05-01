package com.nightcatchers.core.domain.model

/**
 * Pet room evolves as trust grows — shifts from Ghostbusters containment cell to
 * a full "If"-style dream world by stage 4.
 */
enum class RoomStage(
    val trustMin: Int,
    val trustMax: Int,
    val label: String,
) {
    HOLDING_PEN(0, 19, "The Holding Pen"),
    COSY_CORNER(20, 39, "The Cosy Corner"),
    BEDROOM(40, 59, "The Bedroom"),
    SANCTUARY(60, 79, "The Sanctuary"),
    DREAM_ROOM(80, 100, "The Dream Room"),
    ;

    companion object {
        fun fromTrust(trust: Int): RoomStage =
            entries.lastOrNull { trust >= it.trustMin } ?: HOLDING_PEN
    }
}
