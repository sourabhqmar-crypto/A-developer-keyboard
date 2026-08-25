package com.example.service

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.repository.CodingKeyboardRepository
import com.example.ui.keyboard.CodingKeyboardView
import com.example.ui.keyboard.KeyAction
import com.example.ui.theme.MyApplicationTheme

class CodingInputMethodService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    private lateinit var repository: CodingKeyboardRepository

    private var currentComposingWord by mutableStateOf("")

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onEvaluateFullscreenMode(): Boolean {
        // Never go into full-screen extract mode, always keep normal docked keyboard UI
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        repository = CodingKeyboardRepository(this)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@CodingInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@CodingInputMethodService)
            setContent {
                MyApplicationTheme {
                    val prefs by repository.keyboardPrefs.collectAsState()
                    val clipItems by repository.clipboardHistory.collectAsState(initial = emptyList())
                    val recentClipTexts = clipItems.take(3).map { it.text }

                    CodingKeyboardView(
                        preferences = prefs,
                        composingWord = currentComposingWord,
                        recentClipboardClips = recentClipTexts,
                        onKeyAction = { action -> handleImeKeyAction(action) },
                        onLayoutModeChange = { mode ->
                            repository.updatePreferences { it.copy(layoutMode = mode) }
                        },
                        onRecentEmojiUsed = { emoji ->
                            repository.addRecentEmoji(emoji)
                        }
                    )
                }
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        updateComposingState()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentComposingWord = ""
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun updateComposingState() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(40, 0)?.toString() ?: ""
        if (before.isNotEmpty() && before.last().isLetterOrDigit()) {
            val lastWord = before.split(Regex("[\\s\\p{Punct}&&[^_]]+")).lastOrNull() ?: ""
            currentComposingWord = lastWord
        } else {
            currentComposingWord = ""
        }
    }

    private fun handleImeKeyAction(action: KeyAction) {
        val ic: InputConnection = currentInputConnection ?: return

        when (action) {
            is KeyAction.InsertText -> {
                val autoClose = repository.keyboardPrefs.value.autoCloseBrackets
                val afterText = ic.getTextAfterCursor(1, 0)?.toString() ?: ""
                val textToInsert = action.text

                if (autoClose && ((textToInsert == ")" && afterText == ")") ||
                                 (textToInsert == "}" && afterText == "}") ||
                                 (textToInsert == "]" && afterText == "]") ||
                                 (textToInsert == "\"" && afterText == "\"") ||
                                 (textToInsert == "'" && afterText == "'"))) {
                    moveCursorBy(ic, 1)
                } else if (autoClose && textToInsert == "{") {
                    ic.commitText("{}", 1)
                    moveCursorBy(ic, -1)
                } else if (autoClose && textToInsert == "(") {
                    ic.commitText("()", 1)
                    moveCursorBy(ic, -1)
                } else if (autoClose && textToInsert == "[") {
                    ic.commitText("[]", 1)
                    moveCursorBy(ic, -1)
                } else if (autoClose && textToInsert == "\"") {
                    ic.commitText("\"\"", 1)
                    moveCursorBy(ic, -1)
                } else if (autoClose && textToInsert == "'") {
                    ic.commitText("''", 1)
                    moveCursorBy(ic, -1)
                } else if (action.text.contains("()") && action.cursorOffset == -1) {
                    ic.commitText("()", 1)
                    moveCursorBy(ic, -1)
                } else if (action.text.contains("{}") && action.cursorOffset == -1) {
                    ic.commitText("{}", 1)
                    moveCursorBy(ic, -1)
                } else if (action.text.contains("[]") && action.cursorOffset == -1) {
                    ic.commitText("[]", 1)
                    moveCursorBy(ic, -1)
                } else if (action.text.contains("\"\"") && action.cursorOffset == -1) {
                    ic.commitText("\"\"", 1)
                    moveCursorBy(ic, -1)
                } else if (action.text.contains("''") && action.cursorOffset == -1) {
                    ic.commitText("''", 1)
                    moveCursorBy(ic, -1)
                } else {
                    ic.commitText(action.text, 1)
                    if (action.cursorOffset != 0) {
                        moveCursorBy(ic, action.cursorOffset)
                    }
                }
                updateComposingState()
            }
            is KeyAction.CommitSuggestion -> {
                if (currentComposingWord.isNotEmpty()) {
                    ic.deleteSurroundingText(currentComposingWord.length, 0)
                }
                ic.commitText(action.word + " ", 1)
                currentComposingWord = ""
            }
            is KeyAction.Backspace -> {
                val selectedText = ic.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    val before = ic.getTextBeforeCursor(1, 0)?.toString() ?: ""
                    val after = ic.getTextAfterCursor(1, 0)?.toString() ?: ""
                    val isPair = (before == "(" && after == ")") ||
                                 (before == "{" && after == "}") ||
                                 (before == "[" && after == "]") ||
                                 (before == "\"" && after == "\"") ||
                                 (before == "'" && after == "'")
                    if (isPair) {
                        ic.deleteSurroundingText(1, 1)
                    } else {
                        ic.deleteSurroundingText(1, 0)
                    }
                } else {
                    ic.commitText("", 1)
                }
                updateComposingState()
            }
            is KeyAction.SwipeDeleteWords -> {
                val before = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
                if (before.isNotEmpty()) {
                    val trimmed = before.trimEnd()
                    val spaceIdx = trimmed.lastIndexOf(' ')
                    val deleteLen = if (spaceIdx >= 0) before.length - spaceIdx else before.length
                    ic.deleteSurroundingText(deleteLen, 0)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
                updateComposingState()
            }
            is KeyAction.DeleteForward -> {
                ic.deleteSurroundingText(0, 1)
                updateComposingState()
            }
            is KeyAction.Enter -> {
                val currentEditorInfo = currentInputEditorInfo
                val imeOptions = currentEditorInfo?.imeOptions ?: 0
                val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(actionId)
                } else {
                    sendKeyChar('\n')
                }
                currentComposingWord = ""
            }
            is KeyAction.Tab -> {
                ic.commitText("    ", 1)
                updateComposingState()
            }
            is KeyAction.Space -> {
                ic.commitText(" ", 1)
                updateComposingState()
            }
            is KeyAction.DoubleSpacePeriod -> {
                val before = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""
                if (before.endsWith(" ")) {
                    ic.deleteSurroundingText(1, 0)
                    ic.commitText(". ", 1)
                } else {
                    ic.commitText(". ", 1)
                }
                currentComposingWord = ""
            }
            is KeyAction.Escape -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE)
            }
            is KeyAction.CursorMove -> {
                moveCursorBy(ic, action.dx)
                updateComposingState()
            }
            is KeyAction.CursorHome -> {
                ic.setSelection(0, 0)
                updateComposingState()
            }
            is KeyAction.CursorEnd -> {
                val before = ic.getTextBeforeCursor(10000, 0)?.length ?: 0
                val after = ic.getTextAfterCursor(10000, 0)?.length ?: 0
                ic.setSelection(before + after, before + after)
                updateComposingState()
            }
            is KeyAction.SelectAll -> {
                ic.performContextMenuAction(android.R.id.selectAll)
            }
            is KeyAction.Copy -> {
                ic.performContextMenuAction(android.R.id.copy)
            }
            is KeyAction.Paste -> {
                ic.performContextMenuAction(android.R.id.paste)
                updateComposingState()
            }
            is KeyAction.PasteClip -> {
                ic.commitText(action.text, 1)
                updateComposingState()
            }
            is KeyAction.Cut -> {
                ic.performContextMenuAction(android.R.id.cut)
                updateComposingState()
            }
            is KeyAction.Undo -> {
                ic.performContextMenuAction(android.R.id.undo)
                updateComposingState()
            }
            is KeyAction.Redo -> {
                ic.performContextMenuAction(android.R.id.redo)
                updateComposingState()
            }
            is KeyAction.HideKeyboard -> {
                requestHideSelf(0)
            }
            is KeyAction.SwitchToImePicker -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
            is KeyAction.ToggleDevMode -> {
                val current = repository.keyboardPrefs.value.devModeEnabled
                repository.updatePreferences { it.copy(devModeEnabled = !current) }
            }
            is KeyAction.ToggleVibration -> {
                val current = repository.keyboardPrefs.value.hapticStrength
                val next = when (current) {
                    com.example.data.model.HapticStrength.OFF -> com.example.data.model.HapticStrength.MEDIUM
                    com.example.data.model.HapticStrength.LIGHT -> com.example.data.model.HapticStrength.MEDIUM
                    com.example.data.model.HapticStrength.MEDIUM -> com.example.data.model.HapticStrength.STRONG
                    com.example.data.model.HapticStrength.STRONG -> com.example.data.model.HapticStrength.OFF
                }
                repository.updatePreferences { it.copy(hapticStrength = next) }
            }
            is KeyAction.ToggleSound -> {
                val current = repository.keyboardPrefs.value.soundType
                val next = when (current) {
                    com.example.data.model.KeyboardSoundType.OFF -> com.example.data.model.KeyboardSoundType.GBOARD_CLICK
                    com.example.data.model.KeyboardSoundType.GBOARD_CLICK -> com.example.data.model.KeyboardSoundType.MECHANICAL
                    com.example.data.model.KeyboardSoundType.MECHANICAL -> com.example.data.model.KeyboardSoundType.MODERN
                    com.example.data.model.KeyboardSoundType.MODERN -> com.example.data.model.KeyboardSoundType.TYPEWRITER
                    com.example.data.model.KeyboardSoundType.TYPEWRITER -> com.example.data.model.KeyboardSoundType.OFF
                }
                repository.updatePreferences { it.copy(soundType = next) }
            }
            else -> {
                // Layout switches handled locally
            }
        }
    }

    private fun moveCursorBy(ic: InputConnection, delta: Int) {
        val before = ic.getTextBeforeCursor(10000, 0)?.length ?: 0
        val after = ic.getTextAfterCursor(10000, 0)?.length ?: 0
        val currentPos = before
        val newPos = (currentPos + delta).coerceIn(0, currentPos + after)
        ic.setSelection(newPos, newPos)
    }
}
