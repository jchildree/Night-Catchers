package com.nightcatchers.core.domain.model

sealed interface PetInteraction {
    data object Feed : PetInteraction
    data object Play : PetInteraction
    data object Train : PetInteraction
    data object Story : PetInteraction
}
