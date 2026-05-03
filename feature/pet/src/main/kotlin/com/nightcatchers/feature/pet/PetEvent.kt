package com.nightcatchers.feature.pet

sealed interface PetEvent {
    data class NavigateToEvolve(val monsterId: String) : PetEvent
}
