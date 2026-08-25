package com.example.data.model

enum class KeyboardLayoutMode(val displayName: String) {
    QWERTY("QWERTY (Alpha)"),
    SYMBOLS_NUMBERS("?123 (Numbers & Symbols)"),
    SYMBOLS_EXTENDED("=\\< (Extended Symbols)"),
    EMOJI("Emoji Picker"),
    TEXT_EDITING("Text Editing D-Pad"),
    NUMPAD("Number Pad"),
    FULL_5ROW("5-Row PC / Hacker"),
    SYMBOLS("Dev Symbols"),
    TERMINAL("Terminal CLI"),
    SNIPPETS("Code Snippets");

    // Backwards compatibility alias
    companion object {
        val COMPACT = QWERTY
        val NUMPAD_HEX = NUMPAD
    }
}

enum class ModifierLockState {
    OFF,
    ACTIVE_ONCE,
    LOCKED
}

enum class KeyboardSoundType(val displayName: String) {
    OFF("Silent"),
    GBOARD_CLICK("Gboard Soft Click"),
    MODERN("Modern Crisp"),
    MECHANICAL("Mechanical Click"),
    TYPEWRITER("Typewriter")
}

enum class HapticStrength(val displayName: String, val durationMs: Long) {
    OFF("Off", 0L),
    LIGHT("Gentle (12ms)", 12L),
    MEDIUM("Crisp (25ms)", 25L),
    STRONG("Strong (45ms)", 45L)
}

enum class KeyboardHeightPreset(val displayName: String, val heightDp: Int) {
    EXTRA_SHORT("Extra-short", 38),
    SHORT("Short", 42),
    MID_SHORT("Mid-short", 46),
    NORMAL("Normal", 50),
    MID_TALL("Mid-tall", 54),
    TALL("Tall", 58),
    EXTRA_TALL("Extra-tall", 64);

    companion object {
        fun fromHeight(heightDp: Int): KeyboardHeightPreset {
            return entries.minByOrNull { kotlin.math.abs(it.heightDp - heightDp) } ?: NORMAL
        }
    }
}

data class KeyboardThemeConfig(
    val id: String,
    val name: String,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val keyColor: Long,
    val keyTextColor: Long,
    val specialKeyColor: Long,
    val specialKeyTextColor: Long,
    val accentColor: Long,
    val symbolHintColor: Long,
    val borderStrokeColor: Long,
    val isDark: Boolean = true
)
