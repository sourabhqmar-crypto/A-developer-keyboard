package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClipboardItem
import com.example.data.model.CodeFile
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.Snippet
import com.example.data.repository.CodingKeyboardRepository
import com.example.data.repository.KeyboardPreferences
import com.example.ui.keyboard.KeyAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CodingKeyboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CodingKeyboardRepository(application)
    private val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val keyboardPrefs: StateFlow<KeyboardPreferences> = repository.keyboardPrefs
    val allSnippets: StateFlow<List<Snippet>> = repository.allSnippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val clipboardHistory: StateFlow<List<ClipboardItem>> = repository.clipboardHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val codeFiles: StateFlow<List<CodeFile>> = repository.codeFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeFile = MutableStateFlow<CodeFile?>(null)
    val activeFile: StateFlow<CodeFile?> = _activeFile.asStateFlow()

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue: StateFlow<TextFieldValue> = _textFieldValue.asStateFlow()

    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()

    init {
        viewModelScope.launch {
            codeFiles.collect { files ->
                if (_activeFile.value == null && files.isNotEmpty()) {
                    selectFile(files.first())
                }
            }
        }
    }

    fun selectFile(file: CodeFile) {
        _activeFile.value = file
        _textFieldValue.value = TextFieldValue(file.content, TextRange(file.content.length))
        undoStack.clear()
        redoStack.clear()
    }

    fun updateTextFieldValue(value: TextFieldValue) {
        if (_textFieldValue.value.text != value.text) {
            undoStack.add(_textFieldValue.value)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
        }
        _textFieldValue.value = value
    }

    fun saveActiveFile(file: CodeFile) {
        viewModelScope.launch {
            repository.saveCodeFile(file)
            _activeFile.value = file
        }
    }

    fun createNewFile(name: String, language: String) {
        viewModelScope.launch {
            val starterContent = when (language.lowercase()) {
                "python" -> "# $name\ndef main():\n    print(\"Hello from Python!\")\n\nif __name__ == '__main__':\n    main()\n"
                "javascript" -> "// $name\nconsole.log(\"Hello from JavaScript!\");\n"
                "kotlin" -> "fun main() {\n    println(\"Hello from Kotlin!\")\n}\n"
                "html" -> "<!DOCTYPE html>\n<html>\n<body>\n  <h1>New Page</h1>\n</body>\n</html>\n"
                "sql" -> "SELECT * FROM users;\n"
                else -> "# $name\n"
            }
            val newFile = CodeFile(name = name, language = language, content = starterContent)
            repository.saveCodeFile(newFile)
            selectFile(newFile)
        }
    }

    fun deleteFile(file: CodeFile) {
        viewModelScope.launch {
            repository.deleteCodeFile(file)
            val currentList = codeFiles.value.filter { it.id != file.id }
            if (currentList.isNotEmpty()) {
                selectFile(currentList.first())
            }
        }
    }

    fun updatePreferences(update: (KeyboardPreferences) -> KeyboardPreferences) {
        repository.updatePreferences(update)
    }

    fun addRecentEmoji(emoji: String) {
        repository.addRecentEmoji(emoji)
    }

    // Snippets actions
    fun addSnippet(snippet: Snippet) {
        viewModelScope.launch { repository.insertSnippet(snippet) }
    }

    fun toggleFavoriteSnippet(snippet: Snippet) {
        viewModelScope.launch { repository.updateSnippet(snippet.copy(isFavorite = !snippet.isFavorite)) }
    }

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch { repository.deleteSnippet(snippet) }
    }

    // Clipboard actions
    fun addClipboardText(text: String) {
        viewModelScope.launch { repository.addClipboardItem(text) }
    }

    fun togglePinClipboard(item: ClipboardItem) {
        viewModelScope.launch { repository.togglePinClipboard(item) }
    }

    fun deleteClipboardItem(item: ClipboardItem) {
        viewModelScope.launch { repository.deleteClipboardItem(item) }
    }

    fun clearUnpinnedClipboard() {
        viewModelScope.launch { repository.clearUnpinnedClipboard() }
    }

    // Handle Keyboard Action in Editor
    fun handleKeyAction(action: KeyAction) {
        val current = _textFieldValue.value
        val text = current.text
        val selection = current.selection

        when (action) {
            is KeyAction.InsertText -> {
                recordUndo(current)
                var insertStr = action.text
                var offset = action.cursorOffset

                // Auto bracket closing check
                if (keyboardPrefs.value.autoCloseBrackets) {
                    val charAfterCursor = if (selection.min < text.length) text[selection.min].toString() else ""
                    
                    // If user types closing bracket right before an existing closing bracket, just step over it
                    if ((insertStr == ")" && charAfterCursor == ")") ||
                        (insertStr == "}" && charAfterCursor == "}") ||
                        (insertStr == "]" && charAfterCursor == "]") ||
                        (insertStr == "\"" && charAfterCursor == "\"") ||
                        (insertStr == "'" && charAfterCursor == "'")) {
                        val newCursor = (selection.min + 1).coerceAtMost(text.length)
                        _textFieldValue.value = TextFieldValue(text, TextRange(newCursor))
                        return
                    }

                    if (insertStr == "(") {
                        insertStr = "()"
                        offset = -1
                    } else if (insertStr == "{") {
                        insertStr = "{}"
                        offset = -1
                    } else if (insertStr == "[") {
                        insertStr = "[]"
                        offset = -1
                    } else if (insertStr == "\"") {
                        insertStr = "\"\""
                        offset = -1
                    } else if (insertStr == "'") {
                        insertStr = "''"
                        offset = -1
                    }
                }

                val newText = text.replaceRange(selection.min, selection.max, insertStr)
                val newCursor = (selection.min + insertStr.length + offset).coerceIn(0, newText.length)
                _textFieldValue.value = TextFieldValue(newText, TextRange(newCursor))
            }
            is KeyAction.CommitSuggestion -> {
                recordUndo(current)
                val beforeCursor = text.substring(0, selection.min)
                val lastWord = beforeCursor.split(Regex("[\\s\\p{Punct}&&[^_]]+")).lastOrNull() ?: ""
                val replaceStart = if (lastWord.isNotEmpty()) (selection.min - lastWord.length).coerceAtLeast(0) else selection.min
                val insertStr = action.word + " "
                val newText = text.substring(0, replaceStart) + insertStr + text.substring(selection.max)
                _textFieldValue.value = TextFieldValue(newText, TextRange(replaceStart + insertStr.length))
            }
            is KeyAction.Backspace -> {
                if (selection.min != selection.max) {
                    recordUndo(current)
                    val newText = text.removeRange(selection.min, selection.max)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min))
                } else if (selection.min > 0) {
                    recordUndo(current)
                    // Check if deleting inside matching pair like () or {}
                    val charBefore = text.getOrNull(selection.min - 1)
                    val charAfter = text.getOrNull(selection.min)
                    val isPair = (charBefore == '(' && charAfter == ')') ||
                            (charBefore == '{' && charAfter == '}') ||
                            (charBefore == '[' && charAfter == ']') ||
                            (charBefore == '"' && charAfter == '"') ||
                            (charBefore == '\'' && charAfter == '\'')

                    val startPos = selection.min - 1
                    val newText = if (isPair) {
                        text.removeRange(startPos, startPos + 2)
                    } else {
                        text.removeRange(startPos, startPos + 1)
                    }
                    _textFieldValue.value = TextFieldValue(newText, TextRange(startPos))
                }
            }
            is KeyAction.SwipeDeleteWords -> {
                recordUndo(current)
                val beforeCursor = text.substring(0, selection.min)
                if (beforeCursor.isNotEmpty()) {
                    val trimmed = beforeCursor.trimEnd()
                    val spaceIdx = trimmed.lastIndexOf(' ')
                    val deleteStart = if (spaceIdx >= 0) spaceIdx else 0
                    val newText = text.substring(0, deleteStart) + text.substring(selection.max)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(deleteStart))
                }
            }
            is KeyAction.DeleteForward -> {
                if (selection.min != selection.max) {
                    recordUndo(current)
                    val newText = text.removeRange(selection.min, selection.max)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min))
                } else if (selection.min < text.length) {
                    recordUndo(current)
                    val newText = text.removeRange(selection.min, selection.min + 1)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min))
                }
            }
            is KeyAction.Enter -> {
                recordUndo(current)
                // Auto-indentation based on current line leading whitespace
                val beforeCursor = text.substring(0, selection.min)
                val currentLine = beforeCursor.lines().lastOrNull() ?: ""
                val leadingWhitespace = currentLine.takeWhile { it == ' ' || it == '\t' }
                val extraIndent = if (currentLine.trimEnd().endsWith("{") || currentLine.trimEnd().endsWith(":")) "    " else ""

                val insertStr = "\n$leadingWhitespace$extraIndent"
                val newText = text.replaceRange(selection.min, selection.max, insertStr)
                val newPos = selection.min + insertStr.length
                _textFieldValue.value = TextFieldValue(newText, TextRange(newPos))
            }
            is KeyAction.Tab -> {
                recordUndo(current)
                val insertStr = "    "
                val newText = text.replaceRange(selection.min, selection.max, insertStr)
                _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + insertStr.length))
            }
            is KeyAction.Space -> {
                recordUndo(current)
                val newText = text.replaceRange(selection.min, selection.max, " ")
                _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + 1))
            }
            is KeyAction.DoubleSpacePeriod -> {
                recordUndo(current)
                val beforeCursor = text.substring(0, selection.min)
                if (beforeCursor.endsWith(" ")) {
                    val newText = text.substring(0, selection.min - 1) + ". " + text.substring(selection.max)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + 1))
                } else {
                    val newText = text.replaceRange(selection.min, selection.max, ". ")
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + 2))
                }
            }
            is KeyAction.CursorMove -> {
                if (action.dy != 0) {
                    val lines = text.lines()
                    var pos = 0
                    var curLineIndex = 0
                    var colIndex = 0
                    for ((idx, line) in lines.withIndex()) {
                        if (selection.start <= pos + line.length) {
                            curLineIndex = idx
                            colIndex = selection.start - pos
                            break
                        }
                        pos += line.length + 1
                    }
                    val targetLineIndex = (curLineIndex + action.dy).coerceIn(0, lines.size - 1)
                    var targetPos = 0
                    for (i in 0 until targetLineIndex) {
                        targetPos += lines[i].length + 1
                    }
                    targetPos += colIndex.coerceIn(0, lines[targetLineIndex].length)
                    _textFieldValue.value = current.copy(selection = TextRange(targetPos))
                } else {
                    val newPos = (selection.start + action.dx).coerceIn(0, text.length)
                    _textFieldValue.value = current.copy(selection = TextRange(newPos))
                }
            }
            is KeyAction.CursorHome -> {
                val beforeCursor = text.substring(0, selection.start)
                val lastNewline = beforeCursor.lastIndexOf('\n')
                val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
                _textFieldValue.value = current.copy(selection = TextRange(lineStart))
            }
            is KeyAction.CursorEnd -> {
                val afterCursor = text.substring(selection.start)
                val nextNewline = afterCursor.indexOf('\n')
                val lineEnd = if (nextNewline == -1) text.length else selection.start + nextNewline
                _textFieldValue.value = current.copy(selection = TextRange(lineEnd))
            }
            is KeyAction.SelectAll -> {
                _textFieldValue.value = current.copy(selection = TextRange(0, text.length))
            }
            is KeyAction.Copy -> {
                if (selection.min != selection.max) {
                    val selectedText = text.substring(selection.min, selection.max)
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", selectedText))
                    addClipboardText(selectedText)
                }
            }
            is KeyAction.Paste -> {
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                    if (pasteText.isNotEmpty()) {
                        recordUndo(current)
                        val newText = text.replaceRange(selection.min, selection.max, pasteText)
                        _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + pasteText.length))
                    }
                }
            }
            is KeyAction.PasteClip -> {
                recordUndo(current)
                val newText = text.replaceRange(selection.min, selection.max, action.text)
                _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min + action.text.length))
            }
            is KeyAction.Cut -> {
                if (selection.min != selection.max) {
                    val selectedText = text.substring(selection.min, selection.max)
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", selectedText))
                    addClipboardText(selectedText)
                    recordUndo(current)
                    val newText = text.removeRange(selection.min, selection.max)
                    _textFieldValue.value = TextFieldValue(newText, TextRange(selection.min))
                }
            }
            is KeyAction.Undo -> {
                if (undoStack.isNotEmpty()) {
                    redoStack.add(current)
                    val previous = undoStack.removeAt(undoStack.size - 1)
                    _textFieldValue.value = previous
                }
            }
            is KeyAction.Redo -> {
                if (redoStack.isNotEmpty()) {
                    undoStack.add(current)
                    val next = redoStack.removeAt(redoStack.size - 1)
                    _textFieldValue.value = next
                }
            }
            is KeyAction.DuplicateLine -> {
                recordUndo(current)
                val lines = text.lines()
                var pos = 0
                for (line in lines) {
                    if (selection.start <= pos + line.length) {
                        val insertStr = "\n$line"
                        val newText = text.substring(0, pos + line.length) + insertStr + text.substring(pos + line.length)
                        _textFieldValue.value = TextFieldValue(newText, TextRange(selection.start + line.length + 1))
                        break
                    }
                    pos += line.length + 1
                }
            }
            is KeyAction.CommentLine -> {
                recordUndo(current)
                val lines = text.lines().toMutableList()
                var pos = 0
                for (i in lines.indices) {
                    val line = lines[i]
                    if (selection.start <= pos + line.length) {
                        lines[i] = if (line.trimStart().startsWith("//")) {
                            line.replaceFirst("// ", "").replaceFirst("//", "")
                        } else {
                            "// $line"
                        }
                        val newText = lines.joinToString("\n")
                        _textFieldValue.value = TextFieldValue(newText, selection)
                        break
                    }
                    pos += line.length + 1
                }
            }
            is KeyAction.ToggleDevMode -> {
                val currentMode = keyboardPrefs.value.devModeEnabled
                updatePreferences { it.copy(devModeEnabled = !currentMode) }
            }
            is KeyAction.ToggleVibration -> {
                val current = keyboardPrefs.value.hapticStrength
                val next = when (current) {
                    com.example.data.model.HapticStrength.OFF -> com.example.data.model.HapticStrength.MEDIUM
                    com.example.data.model.HapticStrength.LIGHT -> com.example.data.model.HapticStrength.MEDIUM
                    com.example.data.model.HapticStrength.MEDIUM -> com.example.data.model.HapticStrength.STRONG
                    com.example.data.model.HapticStrength.STRONG -> com.example.data.model.HapticStrength.OFF
                }
                updatePreferences { it.copy(hapticStrength = next) }
            }
            is KeyAction.ToggleSound -> {
                val current = keyboardPrefs.value.soundType
                val next = when (current) {
                    com.example.data.model.KeyboardSoundType.OFF -> com.example.data.model.KeyboardSoundType.GBOARD_CLICK
                    com.example.data.model.KeyboardSoundType.GBOARD_CLICK -> com.example.data.model.KeyboardSoundType.MECHANICAL
                    com.example.data.model.KeyboardSoundType.MECHANICAL -> com.example.data.model.KeyboardSoundType.MODERN
                    com.example.data.model.KeyboardSoundType.MODERN -> com.example.data.model.KeyboardSoundType.TYPEWRITER
                    com.example.data.model.KeyboardSoundType.TYPEWRITER -> com.example.data.model.KeyboardSoundType.OFF
                }
                updatePreferences { it.copy(soundType = next) }
            }
            else -> {}
        }
    }

    private fun recordUndo(current: TextFieldValue) {
        undoStack.add(current)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
    }
}
