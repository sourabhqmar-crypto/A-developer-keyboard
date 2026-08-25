package com.example.ui.keyboard

import com.example.data.model.KeyboardLayoutMode

sealed interface KeyAction {
    data class InsertText(val text: String, val cursorOffset: Int = 0) : KeyAction
    data class CommitSuggestion(val word: String) : KeyAction
    data object Backspace : KeyAction
    data class SwipeDeleteWords(val count: Int = 1) : KeyAction
    data object DeleteForward : KeyAction
    data object Enter : KeyAction
    data object Tab : KeyAction
    data object Space : KeyAction
    data object DoubleSpacePeriod : KeyAction
    data object Escape : KeyAction
    data object ToggleShift : KeyAction
    data object ToggleCtrl : KeyAction
    data object ToggleAlt : KeyAction
    data object ToggleFn : KeyAction
    data class SwitchLayout(val mode: KeyboardLayoutMode) : KeyAction
    data object SwitchToImePicker : KeyAction
    data class CursorMove(val dx: Int, val dy: Int) : KeyAction
    data object CursorHome : KeyAction
    data object CursorEnd : KeyAction
    data object PageUp : KeyAction
    data object PageDown : KeyAction
    data object SelectAll : KeyAction
    data object StartSelection : KeyAction
    data object Copy : KeyAction
    data object Paste : KeyAction
    data class PasteClip(val text: String) : KeyAction
    data object Cut : KeyAction
    data object Undo : KeyAction
    data object Redo : KeyAction
    data object CommentLine : KeyAction
    data object DuplicateLine : KeyAction
    data object OpenSnippets : KeyAction
    data object OpenClipboard : KeyAction
    data object OpenEmoji : KeyAction
    data object OpenTextEditing : KeyAction
    data object OpenNumpad : KeyAction
    data object OpenSettings : KeyAction
    data object HideKeyboard : KeyAction
    data object ToggleDevMode : KeyAction
    data object ToggleVibration : KeyAction
    data object ToggleSound : KeyAction
}

data class KeyModel(
    val primaryLabel: String,
    val secondaryLabel: String? = null,
    val action: KeyAction,
    val secondaryAction: KeyAction? = null,
    val weight: Float = 1.0f,
    val isSpecial: Boolean = false,
    val isModifier: Boolean = false,
    val iconName: String? = null,
    val popupOptions: List<String> = emptyList()
)
