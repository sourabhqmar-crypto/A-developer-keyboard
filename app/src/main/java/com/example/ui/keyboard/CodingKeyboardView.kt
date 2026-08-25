package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.ModifierLockState
import com.example.data.repository.KeyboardPreferences
import com.example.ui.theme.KeyboardThemes
import com.example.util.ImeSetupHelper
import kotlin.math.abs

@Composable
fun CodingKeyboardView(
    preferences: KeyboardPreferences,
    onKeyAction: (KeyAction) -> Unit,
    modifier: Modifier = Modifier,
    composingWord: String = "",
    recentClipboardClips: List<String> = emptyList(),
    onLayoutModeChange: ((KeyboardLayoutMode) -> Unit)? = null,
    onRecentEmojiUsed: ((String) -> Unit)? = null,
    onOpenThemePicker: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val feedbackManager = remember { FeedbackManager(context) }
    val theme = remember(preferences.themeId) { KeyboardThemes.getThemeById(preferences.themeId) }

    var isShifted by remember { mutableStateOf(false) }
    var isCapsLocked by remember { mutableStateOf(false) }
    var lastShiftTapTime by remember { mutableStateOf(0L) }

    var ctrlLockState by remember { mutableStateOf(ModifierLockState.OFF) }
    var altLockState by remember { mutableStateOf(ModifierLockState.OFF) }
    var fnActive by remember { mutableStateOf(false) }

    var currentLayoutMode by remember(preferences.layoutMode) { mutableStateOf(preferences.layoutMode) }
    var isToolbarExpanded by remember { mutableStateOf(false) }
    var currentComposingText by remember { mutableStateOf(composingWord) }

    // Sync external composing word
    LaunchedEffect(composingWord) {
        currentComposingText = composingWord
    }

    val suggestions = remember(currentComposingText) {
        WordPredictor.getSuggestions(currentComposingText)
    }

    val handleAction: (KeyAction) -> Unit = { action ->
        feedbackManager.playFeedback(preferences.hapticStrength, preferences.soundType, view)

        when (action) {
            is KeyAction.ToggleShift -> {
                val now = System.currentTimeMillis()
                if (now - lastShiftTapTime < 350) {
                    // Double tap: toggle CAPS LOCK
                    isCapsLocked = !isCapsLocked
                    isShifted = isCapsLocked
                } else {
                    if (isCapsLocked) {
                        isCapsLocked = false
                        isShifted = false
                    } else {
                        isShifted = !isShifted
                    }
                }
                lastShiftTapTime = now
            }
            is KeyAction.ToggleCtrl -> {
                ctrlLockState = when (ctrlLockState) {
                    ModifierLockState.OFF -> ModifierLockState.ACTIVE_ONCE
                    ModifierLockState.ACTIVE_ONCE -> ModifierLockState.LOCKED
                    ModifierLockState.LOCKED -> ModifierLockState.OFF
                }
            }
            is KeyAction.ToggleAlt -> {
                altLockState = when (altLockState) {
                    ModifierLockState.OFF -> ModifierLockState.ACTIVE_ONCE
                    ModifierLockState.ACTIVE_ONCE -> ModifierLockState.LOCKED
                    ModifierLockState.LOCKED -> ModifierLockState.OFF
                }
            }
            is KeyAction.ToggleFn -> {
                fnActive = !fnActive
            }
            is KeyAction.SwitchLayout -> {
                currentLayoutMode = action.mode
                onLayoutModeChange?.invoke(action.mode)
            }
            is KeyAction.SwitchToImePicker -> {
                ImeSetupHelper.showImePicker(context)
                onKeyAction(action)
            }
            is KeyAction.OpenEmoji -> {
                // Emoji picker removed per request; keep fallback if called
                onKeyAction(action)
            }
            is KeyAction.OpenTextEditing -> {
                currentLayoutMode = KeyboardLayoutMode.TEXT_EDITING
                onLayoutModeChange?.invoke(KeyboardLayoutMode.TEXT_EDITING)
            }
            is KeyAction.OpenNumpad -> {
                currentLayoutMode = KeyboardLayoutMode.NUMPAD
                onLayoutModeChange?.invoke(KeyboardLayoutMode.NUMPAD)
            }
            is KeyAction.InsertText -> {
                // Check if Ctrl is active for shortcuts (Ctrl+C, Ctrl+V, Ctrl+A, Ctrl+Z, etc.)
                if (ctrlLockState != ModifierLockState.OFF) {
                    when (action.text.lowercase()) {
                        "c" -> onKeyAction(KeyAction.Copy)
                        "v" -> onKeyAction(KeyAction.Paste)
                        "x" -> onKeyAction(KeyAction.Cut)
                        "a" -> onKeyAction(KeyAction.SelectAll)
                        "z" -> onKeyAction(KeyAction.Undo)
                        "y" -> onKeyAction(KeyAction.Redo)
                        "d" -> onKeyAction(KeyAction.DuplicateLine)
                        "/" -> onKeyAction(KeyAction.CommentLine)
                        else -> onKeyAction(action)
                    }
                    if (ctrlLockState == ModifierLockState.ACTIVE_ONCE) {
                        ctrlLockState = ModifierLockState.OFF
                    }
                } else {
                    onKeyAction(action)
                }

                // If shift was active for single character and not caps locked, turn it off
                if (isShifted && !isCapsLocked) {
                    isShifted = false
                }
            }
            is KeyAction.CommitSuggestion -> {
                onKeyAction(action)
                currentComposingText = ""
            }
            else -> {
                onKeyAction(action)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("coding_keyboard_container"),
        color = Color(theme.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 1. Gboard Toolbar (Suggestions Bar + Quick Tool Actions)
            if (preferences.showSuggestionBar && currentLayoutMode != KeyboardLayoutMode.EMOJI && currentLayoutMode != KeyboardLayoutMode.TEXT_EDITING) {
                GboardToolbar(
                    theme = theme,
                    suggestions = suggestions,
                    composingWord = currentComposingText,
                    recentClips = recentClipboardClips,
                    isExpanded = isToolbarExpanded,
                    isDevMode = preferences.devModeEnabled,
                    hapticStrength = preferences.hapticStrength,
                    soundType = preferences.soundType,
                    onToggleExpand = { isToolbarExpanded = !isToolbarExpanded },
                    onSuggestionClick = { word ->
                        handleAction(KeyAction.CommitSuggestion(word))
                    },
                    onClipClick = { clip ->
                        handleAction(KeyAction.PasteClip(clip))
                    },
                    onToolAction = { act ->
                        when (act) {
                            "emoji" -> handleAction(KeyAction.OpenEmoji)
                            "edit" -> handleAction(KeyAction.OpenTextEditing)
                            "numpad" -> handleAction(KeyAction.OpenNumpad)
                            "clipboard" -> onKeyAction(KeyAction.OpenClipboard)
                            "snippets" -> onKeyAction(KeyAction.OpenSnippets)
                            "theme" -> onOpenThemePicker?.invoke() ?: onKeyAction(KeyAction.OpenSettings)
                            "settings" -> onKeyAction(KeyAction.OpenSettings)
                            "vibrate" -> onKeyAction(KeyAction.ToggleVibration)
                            "sound" -> onKeyAction(KeyAction.ToggleSound)
                            "hide" -> onKeyAction(KeyAction.HideKeyboard)
                            "dev_toggle" -> onKeyAction(KeyAction.ToggleDevMode)
                            "5row" -> handleAction(KeyAction.SwitchLayout(KeyboardLayoutMode.FULL_5ROW))
                            "sym" -> handleAction(KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS))
                            "cli" -> handleAction(KeyAction.SwitchLayout(KeyboardLayoutMode.TERMINAL))
                        }
                    }
                )
            }

            // 2. Developer Quick Symbol Strip (If Dev Mode is Enabled & in QWERTY / 5-Row mode)
            if (preferences.devModeEnabled && (currentLayoutMode == KeyboardLayoutMode.QWERTY || currentLayoutMode == KeyboardLayoutMode.FULL_5ROW)) {
                DevQuickSymbolStrip(
                    theme = theme,
                    symbols = preferences.customQuickSymbols,
                    onSymbolClick = { symbol ->
                        val action = when (symbol) {
                            "Tab" -> KeyAction.Tab
                            "Esc" -> KeyAction.Escape
                            "()" -> KeyAction.InsertText("()", -1)
                            "{}" -> KeyAction.InsertText("{}", -1)
                            "[]" -> KeyAction.InsertText("[]", -1)
                            "\"\"" -> KeyAction.InsertText("\"\"", -1)
                            "''" -> KeyAction.InsertText("''", -1)
                            else -> KeyAction.InsertText(symbol)
                        }
                        handleAction(action)
                    }
                )
            }

            // 3. Main Keyboard Layout Content
            when (currentLayoutMode) {
                KeyboardLayoutMode.EMOJI -> {
                    EmojiKeyboardPicker(
                        theme = theme,
                        recentEmojis = preferences.recentEmojis,
                        onEmojiSelect = { emoji ->
                            onRecentEmojiUsed?.invoke(emoji)
                            handleAction(KeyAction.InsertText(emoji))
                        },
                        onBackspace = { handleAction(KeyAction.Backspace) },
                        onSwitchToAbc = { handleAction(KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY)) }
                    )
                }
                KeyboardLayoutMode.TEXT_EDITING -> {
                    TextEditingPad(
                        theme = theme,
                        onAction = handleAction,
                        onSwitchToAbc = { handleAction(KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY)) }
                    )
                }
                KeyboardLayoutMode.NUMPAD -> {
                    val rows = KeyboardLayouts.getNumpadLayout()
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = false,
                        isCapsLocked = false,
                        ctrlLockState = ModifierLockState.OFF,
                        altLockState = ModifierLockState.OFF,
                        fnActive = false,
                        onAction = handleAction
                    )
                }
                KeyboardLayoutMode.SYMBOLS_NUMBERS -> {
                    val rows = KeyboardLayouts.getGboardSymbols123(preferences.showNumberRow)
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = false,
                        isCapsLocked = false,
                        ctrlLockState = ModifierLockState.OFF,
                        altLockState = ModifierLockState.OFF,
                        fnActive = false,
                        onAction = handleAction
                    )
                }
                KeyboardLayoutMode.SYMBOLS_EXTENDED -> {
                    val rows = KeyboardLayouts.getGboardExtendedSymbols()
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = false,
                        isCapsLocked = false,
                        ctrlLockState = ModifierLockState.OFF,
                        altLockState = ModifierLockState.OFF,
                        fnActive = false,
                        onAction = handleAction
                    )
                }
                KeyboardLayoutMode.FULL_5ROW -> {
                    val rows = KeyboardLayouts.getFull5RowLayout(
                        isShifted = isShifted || isCapsLocked,
                        isCtrlActive = ctrlLockState != ModifierLockState.OFF,
                        isAltActive = altLockState != ModifierLockState.OFF,
                        isFnActive = fnActive
                    )
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = isShifted,
                        isCapsLocked = isCapsLocked,
                        ctrlLockState = ctrlLockState,
                        altLockState = altLockState,
                        fnActive = fnActive,
                        onAction = handleAction
                    )
                }
                KeyboardLayoutMode.SYMBOLS -> {
                    val rows = KeyboardLayouts.getSymbolsLayout()
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = false,
                        isCapsLocked = false,
                        ctrlLockState = ModifierLockState.OFF,
                        altLockState = ModifierLockState.OFF,
                        fnActive = false,
                        onAction = handleAction
                    )
                }
                KeyboardLayoutMode.TERMINAL -> {
                    val rows = KeyboardLayouts.getTerminalLayout()
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = false,
                        isCapsLocked = false,
                        ctrlLockState = ModifierLockState.OFF,
                        altLockState = ModifierLockState.OFF,
                        fnActive = false,
                        onAction = handleAction
                    )
                }
                else -> {
                    // Standard Gboard QWERTY (Alpha)
                    val rows = KeyboardLayouts.getGboardQwerty(
                        isShifted = isShifted,
                        isCapsLocked = isCapsLocked,
                        showNumberRow = preferences.showNumberRow
                    )
                    renderKeyRows(
                        rows = rows,
                        theme = theme,
                        preferences = preferences,
                        isShifted = isShifted,
                        isCapsLocked = isCapsLocked,
                        ctrlLockState = ctrlLockState,
                        altLockState = altLockState,
                        fnActive = fnActive,
                        onAction = handleAction
                    )
                }
            }
        }
    }
}

@Composable
private fun renderKeyRows(
    rows: List<List<KeyModel>>,
    theme: KeyboardThemeConfig,
    preferences: KeyboardPreferences,
    isShifted: Boolean,
    isCapsLocked: Boolean,
    ctrlLockState: ModifierLockState,
    altLockState: ModifierLockState,
    fnActive: Boolean,
    onAction: (KeyAction) -> Unit
) {
    rows.forEach { rowKeys ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            rowKeys.forEach { keyModel ->
                val modifierState = when (keyModel.action) {
                    is KeyAction.ToggleShift -> if (isCapsLocked) ModifierLockState.LOCKED else if (isShifted) ModifierLockState.ACTIVE_ONCE else ModifierLockState.OFF
                    is KeyAction.ToggleCtrl -> ctrlLockState
                    is KeyAction.ToggleAlt -> altLockState
                    else -> ModifierLockState.OFF
                }

                GboardKeyCapView(
                    keyModel = keyModel,
                    theme = theme,
                    keyHeightDp = preferences.keyHeightDp,
                    showSecondaryHints = preferences.showSecondaryHints,
                    showKeyBorders = preferences.showKeyBorders,
                    showKeyPopup = preferences.showKeyPopup,
                    modifierState = modifierState,
                    isFnActive = fnActive && keyModel.action is KeyAction.ToggleFn,
                    spacebarCursorSwipe = preferences.spacebarCursorSwipe,
                    backspaceSwipeDelete = preferences.backspaceSwipeDelete,
                    doubleSpacePeriod = preferences.doubleSpacePeriod,
                    onAction = onAction,
                    modifier = Modifier.weight(keyModel.weight)
                )
            }
        }
    }
}

@Composable
fun GboardToolbar(
    theme: KeyboardThemeConfig,
    suggestions: List<String>,
    composingWord: String,
    recentClips: List<String>,
    isExpanded: Boolean,
    isDevMode: Boolean,
    hapticStrength: com.example.data.model.HapticStrength = com.example.data.model.HapticStrength.MEDIUM,
    soundType: com.example.data.model.KeyboardSoundType = com.example.data.model.KeyboardSoundType.GBOARD_CLICK,
    onToggleExpand: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onClipClick: (String) -> Unit,
    onToolAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(theme.surfaceColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand / Collapse Chevron Button (Gboard 4-dots or Arrow)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleExpand),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.MoreHoriz,
                    contentDescription = "Expand Toolbar",
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!isExpanded) {
                // Word Suggestions Bar with Clipboard Chip if available
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // If recent clip is available, show as 1st suggestion chip
                    if (recentClips.isNotEmpty() && composingWord.isEmpty()) {
                        val clip = recentClips.first()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(theme.accentColor).copy(alpha = 0.2f))
                                .clickable { onClipClick(clip) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Paste",
                                    tint = Color(theme.accentColor),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = clip.take(12) + if (clip.length > 12) "…" else "",
                                    color = Color(theme.accentColor),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Predictive suggestions
                    suggestions.forEachIndexed { index, suggestion ->
                        val isCenterPrimary = index == 1 || (suggestions.size == 1)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = suggestion,
                                color = if (isCenterPrimary) Color(theme.accentColor) else Color(theme.keyTextColor),
                                fontSize = if (isCenterPrimary) 14.sp else 13.sp,
                                fontWeight = if (isCenterPrimary) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (index < suggestions.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(16.dp)
                                    .background(Color(theme.borderStrokeColor))
                            )
                        }
                    }
                }

                // Quick 1-tap Vibration & Sound Toggle Buttons on right of toolbar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    val isVibrateOn = hapticStrength != com.example.data.model.HapticStrength.OFF
                    val isSoundOn = soundType != com.example.data.model.KeyboardSoundType.OFF

                    // Vibration Toggle Quick Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isVibrateOn) Color(theme.accentColor).copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onToolAction("vibrate") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = if (isVibrateOn) "Vibrate On (${hapticStrength.name})" else "Vibrate Off",
                            tint = if (isVibrateOn) Color(theme.accentColor) else Color(theme.keyTextColor).copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Keypress Sound Toggle Quick Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isSoundOn) Color(theme.accentColor).copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onToolAction("sound") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isSoundOn) "Sound On" else "Sound Off",
                            tint = if (isSoundOn) Color(theme.accentColor) else Color(theme.keyTextColor).copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            } else {
                // Expanded Action Bar (Emoji, Text Edit, Vibration, Sound, Clipboard, Snippets, Numpad, Themes, Settings)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isVibrateOn = hapticStrength != com.example.data.model.HapticStrength.OFF
                    val isSoundOn = soundType != com.example.data.model.KeyboardSoundType.OFF

                    ToolbarActionChip(
                        icon = Icons.Default.Vibration,
                        label = if (isVibrateOn) "Vibrate: ${hapticStrength.name.lowercase().replaceFirstChar { it.uppercase() }}" else "Vibrate: Off",
                        theme = theme
                    ) { onToolAction("vibrate") }

                    ToolbarActionChip(
                        icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = if (isSoundOn) "Sound: ${soundType.name.lowercase().replaceFirstChar { it.uppercase() }}" else "Sound: Off",
                        theme = theme
                    ) { onToolAction("sound") }

                    ToolbarActionChip(icon = Icons.Default.Language, label = "Switch Keyboard", theme = theme) { onToolAction("ime_picker") }
                    ToolbarActionChip(icon = Icons.Default.AspectRatio, label = "Resize", theme = theme) { onToolAction("settings") }
                    ToolbarActionChip(icon = Icons.Default.EditNote, label = "Text Edit", theme = theme) { onToolAction("edit") }
                    ToolbarActionChip(icon = Icons.Default.Assignment, label = "Clipboard", theme = theme) { onToolAction("clipboard") }
                    ToolbarActionChip(icon = Icons.Default.Numbers, label = "Numpad", theme = theme) { onToolAction("numpad") }
                    ToolbarActionChip(icon = Icons.Default.ColorLens, label = "Themes", theme = theme) { onToolAction("theme") }
                    ToolbarActionChip(icon = Icons.Default.Code, label = "Snippets", theme = theme) { onToolAction("snippets") }
                    ToolbarActionChip(icon = Icons.Default.Settings, label = "Settings", theme = theme) { onToolAction("settings") }
                }
            }
        }
    }
}

@Composable
private fun ToolbarActionChip(
    icon: ImageVector,
    label: String,
    theme: KeyboardThemeConfig,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(theme.keyColor))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color(theme.accentColor), modifier = Modifier.size(16.dp))
        Text(text = label, color = Color(theme.keyTextColor), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DevQuickSymbolStrip(
    theme: KeyboardThemeConfig,
    symbols: List<String>,
    onSymbolClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        symbols.forEach { sym ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(theme.surfaceColor))
                    .clickable { onSymbolClick(sym) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sym,
                    color = Color(theme.keyTextColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GboardKeyCapView(
    keyModel: KeyModel,
    theme: KeyboardThemeConfig,
    keyHeightDp: Int,
    showSecondaryHints: Boolean,
    showKeyBorders: Boolean,
    showKeyPopup: Boolean,
    modifierState: ModifierLockState,
    isFnActive: Boolean,
    spacebarCursorSwipe: Boolean,
    backspaceSwipeDelete: Boolean,
    doubleSpacePeriod: Boolean,
    onAction: (KeyAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var spaceSwipeAccumulator by remember { mutableFloatStateOf(0f) }
    var backspaceSwipeAccumulator by remember { mutableFloatStateOf(0f) }
    var showPopupOptions by remember { mutableStateOf(false) }
    var lastSpaceTapTime by remember { mutableStateOf(0L) }

    val keyBg = when {
        isPressed -> Color(theme.accentColor).copy(alpha = 0.35f)
        modifierState == ModifierLockState.LOCKED -> Color(theme.accentColor)
        modifierState == ModifierLockState.ACTIVE_ONCE -> Color(theme.accentColor).copy(alpha = 0.6f)
        isFnActive -> Color(theme.accentColor)
        keyModel.isSpecial || keyModel.isModifier -> Color(theme.specialKeyColor)
        else -> Color(theme.keyColor)
    }

    val textCol = when {
        modifierState == ModifierLockState.LOCKED -> Color.White
        keyModel.isSpecial || keyModel.isModifier -> Color(theme.specialKeyTextColor)
        else -> Color(theme.keyTextColor)
    }

    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "key_scale")

    Box(
        modifier = modifier
            .height(keyHeightDp.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(keyBg)
            .then(
                if (showKeyBorders) {
                    Modifier.border(0.5.dp, Color(theme.borderStrokeColor), RoundedCornerShape(6.dp))
                } else Modifier
            )
            .pointerInput(keyModel, spacebarCursorSwipe, doubleSpacePeriod, backspaceSwipeDelete) {
                if (keyModel.action is KeyAction.Backspace && backspaceSwipeDelete) {
                    detectDragGestures(
                        onDragStart = { isPressed = true; backspaceSwipeAccumulator = 0f },
                        onDragEnd = { isPressed = false },
                        onDragCancel = { isPressed = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            backspaceSwipeAccumulator += dragAmount.x
                            val threshold = -40f
                            if (backspaceSwipeAccumulator <= threshold) {
                                onAction(KeyAction.SwipeDeleteWords(1))
                                backspaceSwipeAccumulator = 0f
                            }
                        }
                    )
                } else if (keyModel.action is KeyAction.Space) {
                    // Spacebar Gestures:
                    // 1. Hold Spacebar (Long-Press) -> Open System Keyboard Picker (Gboard style)
                    // 2. Drag Horizontally -> Cursor Glide Left / Right
                    // 3. Tap -> Insert Space / Double-tap Period
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        var totalDragX = 0f
                        var isDragging = false
                        var longPressTriggered = false
                        val startTime = System.currentTimeMillis()
                        val longPressTimeout = 320L

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                isPressed = false
                                if (!isDragging && !longPressTriggered) {
                                    if (doubleSpacePeriod) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastSpaceTapTime < 300) {
                                            onAction(KeyAction.DoubleSpacePeriod)
                                        } else {
                                            onAction(KeyAction.Space)
                                        }
                                        lastSpaceTapTime = now
                                    } else {
                                        onAction(KeyAction.Space)
                                    }
                                }
                                break
                            }

                            val dxFromDown = change.position.x - down.position.x
                            val dyFromDown = change.position.y - down.position.y
                            val dist = kotlin.math.sqrt(dxFromDown * dxFromDown + dyFromDown * dyFromDown)

                            if (dist > 16f && spacebarCursorSwipe && !longPressTriggered) {
                                isDragging = true
                                val dx = change.positionChange().x
                                totalDragX += dx
                                val threshold = 25f
                                if (kotlin.math.abs(totalDragX) >= threshold) {
                                    val step = if (totalDragX > 0) 1 else -1
                                    onAction(KeyAction.CursorMove(step, 0))
                                    totalDragX = 0f
                                }
                                change.consume()
                            } else if (!isDragging && !longPressTriggered && (System.currentTimeMillis() - startTime >= longPressTimeout)) {
                                longPressTriggered = true
                                onAction(KeyAction.SwitchToImePicker)
                                change.consume()
                            }
                        }
                        isPressed = false
                    }
                } else {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = {
                            onAction(keyModel.action)
                        },
                        onLongPress = {
                            if (keyModel.popupOptions.size > 1) {
                                showPopupOptions = true
                            } else if (keyModel.secondaryAction != null) {
                                onAction(keyModel.secondaryAction)
                            } else {
                                onAction(keyModel.action)
                            }
                        }
                    )
                }
            }
            .testTag("key_${keyModel.primaryLabel}"),
        contentAlignment = Alignment.Center
    ) {
        // Gboard Magnified Character Preview Popup (When touched)
        if (isPressed && showKeyPopup && !showPopupOptions && keyModel.primaryLabel.length <= 2 && !keyModel.isModifier) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -110)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp, 60.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(theme.surfaceColor))
                        .border(1.dp, Color(theme.accentColor), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = keyModel.primaryLabel,
                        color = Color(theme.accentColor),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Long Press Flyout Popup for Accents / Extra symbols
        if (showPopupOptions && keyModel.popupOptions.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -110),
                onDismissRequest = { showPopupOptions = false }
            ) {
                Row(
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(theme.surfaceColor))
                        .border(1.dp, Color(theme.borderStrokeColor), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    keyModel.popupOptions.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(theme.keyColor))
                                .clickable {
                                    onAction(KeyAction.InsertText(opt))
                                    showPopupOptions = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                color = Color(theme.keyTextColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Secondary Hint at top-right
        if (showSecondaryHints && keyModel.secondaryLabel != null) {
            Text(
                text = keyModel.secondaryLabel,
                color = Color(theme.symbolHintColor),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 3.dp)
            )
        }

        // Lock indicator dot for Shift / Caps
        if (modifierState != ModifierLockState.OFF) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (modifierState == ModifierLockState.LOCKED) Color.Yellow else Color.Cyan)
            )
        }

        // Primary Key Label
        Text(
            text = keyModel.primaryLabel,
            color = textCol,
            fontSize = when {
                keyModel.primaryLabel.length > 4 -> 10.sp
                keyModel.primaryLabel.length > 2 -> 12.sp
                else -> 16.sp
            },
            fontWeight = if (keyModel.isSpecial || keyModel.isModifier) FontWeight.Bold else FontWeight.Medium,
            fontFamily = if (keyModel.primaryLabel.length == 1 && keyModel.primaryLabel.first().isLetter()) FontFamily.Default else FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}
