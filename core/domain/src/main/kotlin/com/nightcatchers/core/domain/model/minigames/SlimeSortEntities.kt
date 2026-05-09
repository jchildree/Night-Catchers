package com.nightcatchers.core.domain.model.minigames

enum class BlobColor(val emoji: String) { RED("🔴"), BLUE("🔵"), GREEN("🟢"), YELLOW("🟡") }
enum class BlobShape(val label: String) { CIRCLE("●"), STAR("★"), HEXAGON("⬡"), TRIANGLE("▲") }

data class BlobEntity(
    val id: String,
    val color: BlobColor,
    val shape: BlobShape,
    val isPhantom: Boolean = false,
)

data class JarEntity(
    val id: String,
    val acceptsColor: BlobColor,
    val acceptsShape: BlobShape,
    val label: String, // e.g., "🔴●"
)
