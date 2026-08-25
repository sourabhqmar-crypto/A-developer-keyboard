package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.ClipboardItem
import com.example.data.model.CodeFile
import com.example.data.model.HapticStrength
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.KeyboardSoundType
import com.example.data.model.Snippet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KeyboardPreferences(
    val themeId: String = "gboard_dark",
    val layoutMode: KeyboardLayoutMode = KeyboardLayoutMode.QWERTY,
    val keyHeightDp: Int = 50,
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val soundType: KeyboardSoundType = KeyboardSoundType.GBOARD_CLICK,
    val longPressDelayMs: Long = 250L,
    val autoCloseBrackets: Boolean = true,
    val showSecondaryHints: Boolean = true,
    val showNumberRow: Boolean = true,
    val showKeyBorders: Boolean = true,
    val showKeyPopup: Boolean = true,
    val showSuggestionBar: Boolean = true,
    val spacebarCursorSwipe: Boolean = true,
    val backspaceSwipeDelete: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val autoCapitalize: Boolean = true,
    val devModeEnabled: Boolean = true,
    val recentEmojis: List<String> = listOf("😀", "😂", "🔥", "👍", "❤️", "🎉", "✨", "🚀", "💻", "⚡", "👀", "🙌"),
    val customQuickSymbols: List<String> = listOf("Tab", "Esc", "(", ")", "{", "}", "[", "]", ";", "=", "->", "=>", "\"", "'", ":", "<", ">", "_", "!", "&", "|", "\\", "$", "*", "?", "/", "+", "-")
)

class CodingKeyboardRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val snippetDao = database.snippetDao()
    private val clipboardDao = database.clipboardDao()
    private val codeFileDao = database.codeFileDao()

    private val prefs: SharedPreferences = context.getSharedPreferences("gboard_coding_keyboard_prefs", Context.MODE_PRIVATE)

    private val _keyboardPrefs = MutableStateFlow(loadPreferences())
    val keyboardPrefs: StateFlow<KeyboardPreferences> = _keyboardPrefs.asStateFlow()

    // Snippets
    val allSnippets: Flow<List<Snippet>> = snippetDao.getAllSnippets()
    fun getSnippetsByLanguage(lang: String): Flow<List<Snippet>> = snippetDao.getSnippetsByLanguage(lang)
    suspend fun insertSnippet(snippet: Snippet) = snippetDao.insertSnippet(snippet)
    suspend fun updateSnippet(snippet: Snippet) = snippetDao.updateSnippet(snippet)
    suspend fun deleteSnippet(snippet: Snippet) = snippetDao.deleteSnippet(snippet)

    // Clipboard
    val clipboardHistory: Flow<List<ClipboardItem>> = clipboardDao.getAllClipboardItems()
    suspend fun addClipboardItem(text: String, isPinned: Boolean = false) {
        if (text.isNotBlank()) {
            clipboardDao.insertItem(ClipboardItem(text = text, isPinned = isPinned))
        }
    }
    suspend fun togglePinClipboard(item: ClipboardItem) {
        clipboardDao.updateItem(item.copy(isPinned = !item.isPinned))
    }
    suspend fun deleteClipboardItem(item: ClipboardItem) = clipboardDao.deleteItem(item)
    suspend fun clearUnpinnedClipboard() = clipboardDao.clearUnpinned()

    // Code Files
    val codeFiles: Flow<List<CodeFile>> = codeFileDao.getAllFiles()
    suspend fun saveCodeFile(file: CodeFile) {
        if (file.id == 0L) {
            codeFileDao.insertFile(file)
        } else {
            codeFileDao.updateFile(file.copy(lastModified = System.currentTimeMillis()))
        }
    }
    suspend fun deleteCodeFile(file: CodeFile) = codeFileDao.deleteFile(file)

    // Preferences
    private fun loadPreferences(): KeyboardPreferences {
        val theme = prefs.getString("theme_id", "gboard_dark") ?: "gboard_dark"
        val layoutName = prefs.getString("layout_mode", KeyboardLayoutMode.QWERTY.name) ?: KeyboardLayoutMode.QWERTY.name
        val layoutMode = runCatching { KeyboardLayoutMode.valueOf(layoutName) }.getOrDefault(KeyboardLayoutMode.QWERTY)
        val keyHeight = prefs.getInt("key_height", 50)
        val hapticName = prefs.getString("haptic_strength", HapticStrength.MEDIUM.name) ?: HapticStrength.MEDIUM.name
        val hapticStrength = runCatching { HapticStrength.valueOf(hapticName) }.getOrDefault(HapticStrength.MEDIUM)
        val soundName = prefs.getString("sound_type", KeyboardSoundType.GBOARD_CLICK.name) ?: KeyboardSoundType.GBOARD_CLICK.name
        val soundType = runCatching { KeyboardSoundType.valueOf(soundName) }.getOrDefault(KeyboardSoundType.GBOARD_CLICK)
        val longPressDelay = prefs.getLong("long_press_delay", 250L)
        val autoClose = prefs.getBoolean("auto_close_brackets", true)
        val showHints = prefs.getBoolean("show_secondary_hints", true)
        val showNumRow = prefs.getBoolean("show_number_row", true)
        val showBorders = prefs.getBoolean("show_key_borders", true)
        val showPopup = prefs.getBoolean("show_key_popup", true)
        val showSuggestions = prefs.getBoolean("show_suggestion_bar", true)
        val spacebarSwipe = prefs.getBoolean("spacebar_cursor_swipe", true)
        val backspaceSwipe = prefs.getBoolean("backspace_swipe_delete", true)
        val doubleSpace = prefs.getBoolean("double_space_period", true)
        val autoCap = prefs.getBoolean("auto_capitalize", true)
        val devMode = prefs.getBoolean("dev_mode_enabled", true)
        
        val emojisStr = prefs.getString("recent_emojis", null)
        val emojisList = emojisStr?.split(",")?.filter { it.isNotEmpty() } ?: listOf("😀", "😂", "🔥", "👍", "❤️", "🎉", "✨", "🚀", "💻", "⚡", "👀", "🙌")

        val symbolsStr = prefs.getString("custom_symbols", null)
        val symbolsList = symbolsStr?.split(",")?.filter { it.isNotEmpty() } ?: listOf("Tab", "Esc", "(", ")", "{", "}", "[", "]", ";", "=", "->", "=>", "\"", "'", ":", "<", ">", "_", "!", "&", "|", "\\", "$", "*", "?", "/", "+", "-")

        return KeyboardPreferences(
            themeId = theme,
            layoutMode = layoutMode,
            keyHeightDp = keyHeight,
            hapticStrength = hapticStrength,
            soundType = soundType,
            longPressDelayMs = longPressDelay,
            autoCloseBrackets = autoClose,
            showSecondaryHints = showHints,
            showNumberRow = showNumRow,
            showKeyBorders = showBorders,
            showKeyPopup = showPopup,
            showSuggestionBar = showSuggestions,
            spacebarCursorSwipe = spacebarSwipe,
            backspaceSwipeDelete = backspaceSwipe,
            doubleSpacePeriod = doubleSpace,
            autoCapitalize = autoCap,
            devModeEnabled = devMode,
            recentEmojis = emojisList,
            customQuickSymbols = symbolsList
        )
    }

    fun addRecentEmoji(emoji: String) {
        val currentList = _keyboardPrefs.value.recentEmojis.toMutableList()
        currentList.remove(emoji)
        currentList.add(0, emoji)
        val trimmed = currentList.take(24)
        updatePreferences { it.copy(recentEmojis = trimmed) }
    }

    fun updatePreferences(update: (KeyboardPreferences) -> KeyboardPreferences) {
        val current = _keyboardPrefs.value
        val updated = update(current)
        _keyboardPrefs.value = updated

        prefs.edit().apply {
            putString("theme_id", updated.themeId)
            putString("layout_mode", updated.layoutMode.name)
            putInt("key_height", updated.keyHeightDp)
            putString("haptic_strength", updated.hapticStrength.name)
            putString("sound_type", updated.soundType.name)
            putLong("long_press_delay", updated.longPressDelayMs)
            putBoolean("auto_close_brackets", updated.autoCloseBrackets)
            putBoolean("show_secondary_hints", updated.showSecondaryHints)
            putBoolean("show_number_row", updated.showNumberRow)
            putBoolean("show_key_borders", updated.showKeyBorders)
            putBoolean("show_key_popup", updated.showKeyPopup)
            putBoolean("show_suggestion_bar", updated.showSuggestionBar)
            putBoolean("spacebar_cursor_swipe", updated.spacebarCursorSwipe)
            putBoolean("backspace_swipe_delete", updated.backspaceSwipeDelete)
            putBoolean("double_space_period", updated.doubleSpacePeriod)
            putBoolean("auto_capitalize", updated.autoCapitalize)
            putBoolean("dev_mode_enabled", updated.devModeEnabled)
            putString("recent_emojis", updated.recentEmojis.joinToString(","))
            putString("custom_symbols", updated.customQuickSymbols.joinToString(","))
            apply()
        }
    }
}
