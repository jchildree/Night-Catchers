package com.nightcatchers.core.domain.model.minigames

enum class FoodItem(val emoji: String, val hungerValue: Int) {
    BUG("🐛", 20),
    SLIMEBALL("🟢", 25),
    GLOW_SNACK("✨", 30),
    MOON_COOKIE("🌙", 35),
    SOCK_BALL("🧦", 15), // Sock Thief's favourite — gives bonus happiness
}
