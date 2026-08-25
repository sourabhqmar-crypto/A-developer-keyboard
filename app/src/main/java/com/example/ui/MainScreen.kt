package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.HapticStrength
import com.example.data.model.KeyboardSoundType
import com.example.ui.clipboard.ClipboardSheet
import com.example.ui.editor.CodeEditorSandbox
import com.example.ui.keyboard.CodingKeyboardView
import com.example.ui.keyboard.KeyAction
import com.example.ui.settings.KeyboardSettingsScreen
import com.example.ui.snippets.SnippetsScreen
import com.example.util.ImeSetupHelper
import kotlinx.coroutines.launch

enum class ActiveSheet {
    NONE, SNIPPETS, CLIPBOARD, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CodingKeyboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val prefs by viewModel.keyboardPrefs.collectAsState()
    val snippets by viewModel.allSnippets.collectAsState()
    val clipboardItems by viewModel.clipboardHistory.collectAsState()
    val codeFiles by viewModel.codeFiles.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val textFieldValue by viewModel.textFieldValue.collectAsState()

    var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val focusRequester = remember { FocusRequester() }

    var isImeEnabled by remember { mutableStateOf(ImeSetupHelper.isImeEnabled(context)) }
    var isImeSelected by remember { mutableStateOf(ImeSetupHelper.isImeSelected(context)) }

    // Recheck system IME registration status on every resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isImeEnabled = ImeSetupHelper.isImeEnabled(context)
                isImeSelected = ImeSetupHelper.isImeSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Derive current typing word before cursor for live suggestions in the sandbox
    val composingWord = remember(textFieldValue.text, textFieldValue.selection) {
        val selMin = textFieldValue.selection.min
        if (selMin > 0 && selMin <= textFieldValue.text.length) {
            val before = textFieldValue.text.substring(0, selMin)
            if (before.isNotEmpty() && before.last().isLetterOrDigit()) {
                before.split(Regex("[\\s\\p{Punct}&&[^_]]+")).lastOrNull() ?: ""
            } else ""
        } else ""
    }

    val recentClips = remember(clipboardItems) {
        clipboardItems.take(3).map { it.text }
    }

    val isVibrationOn = prefs.hapticStrength != HapticStrength.OFF
    val isSoundOn = prefs.soundType != KeyboardSoundType.OFF

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Top Header with Title, Vibration Toggle, Sound Toggle, and Quick Action Buttons
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // App Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Coding Keyboard",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (prefs.autoCloseBrackets) "Auto-bracket { ( [ enabled" else "Full IDE Virtual Keyboard",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Top Action Controls: Vibration, Sound, Snippets, Clipboard, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Vibration Toggle Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isVibrationOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.handleKeyAction(KeyAction.ToggleVibration) }
                                .testTag("vibration_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Toggle Vibration",
                                    tint = if (isVibrationOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isVibrationOn) prefs.hapticStrength.name.take(3) else "Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isVibrationOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sound Toggle Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSoundOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.handleKeyAction(KeyAction.ToggleSound) }
                                .testTag("sound_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle Sound",
                                    tint = if (isSoundOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isSoundOn) "On" else "Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSoundOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Snippets Button
                        IconButton(
                            onClick = { activeSheet = ActiveSheet.SNIPPETS },
                            modifier = Modifier.size(32.dp).testTag("header_snippets_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Snippets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Clipboard Button
                        IconButton(
                            onClick = { activeSheet = ActiveSheet.CLIPBOARD },
                            modifier = Modifier.size(32.dp).testTag("header_clipboard_button")
                        ) {
                            if (clipboardItems.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${clipboardItems.size}") } }) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = "Clipboard",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Clipboard",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Settings Button
                        IconButton(
                            onClick = { activeSheet = ActiveSheet.SETTINGS },
                            modifier = Modifier.size(32.dp).testTag("header_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. System-Wide IME Quick Setup Banner (if not yet enabled/selected)
            if (!isImeEnabled || !isImeSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isImeEnabled) {
                                ImeSetupHelper.openImeSettings(context)
                            } else {
                                ImeSetupHelper.showImePicker(context)
                            }
                        }
                        .testTag("ime_setup_top_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (!isImeEnabled) "Tap to Enable Coding Keyboard in System Settings" else "Tap to Select as Active System Keyboard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Activate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 3. Main Workspace: Code Sandbox Playground (Flex)
            CodeEditorSandbox(
                files = codeFiles,
                activeFile = activeFile,
                onSelectFile = { viewModel.selectFile(it) },
                onSaveFile = { viewModel.saveActiveFile(it) },
                onDeleteFile = { viewModel.deleteFile(it) },
                onCreateFile = { name, lang -> viewModel.createNewFile(name, lang) },
                preferences = prefs,
                textFieldValue = textFieldValue,
                onTextFieldValueChange = { viewModel.updateTextFieldValue(it) },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f)
            )

            // 4. Interactive Live Virtual Coding Keyboard (Attached directly at bottom)
            CodingKeyboardView(
                preferences = prefs,
                composingWord = composingWord,
                recentClipboardClips = recentClips,
                onKeyAction = { action ->
                    when (action) {
                        is KeyAction.OpenSettings -> activeSheet = ActiveSheet.SETTINGS
                        is KeyAction.OpenClipboard -> activeSheet = ActiveSheet.CLIPBOARD
                        is KeyAction.OpenSnippets -> activeSheet = ActiveSheet.SNIPPETS
                        is KeyAction.SwitchToImePicker -> {
                            ImeSetupHelper.showImePicker(context)
                        }
                        else -> viewModel.handleKeyAction(action)
                    }
                },
                onLayoutModeChange = { mode ->
                    viewModel.updatePreferences { it.copy(layoutMode = mode) }
                },
                onRecentEmojiUsed = { emoji ->
                    viewModel.addRecentEmoji(emoji)
                },
                onOpenThemePicker = {
                    activeSheet = ActiveSheet.SETTINGS
                }
            )
        }
    }

    // Modal Bottom Sheets for Overlay Panels (Snippets, Clipboard, Settings)
    if (activeSheet != ActiveSheet.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = ActiveSheet.NONE },
            sheetState = sheetState
        ) {
            when (activeSheet) {
                ActiveSheet.SNIPPETS -> {
                    SnippetsScreen(
                        snippets = snippets,
                        onInsertSnippet = { code ->
                            viewModel.handleKeyAction(KeyAction.InsertText(code))
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                activeSheet = ActiveSheet.NONE
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavoriteSnippet(it) },
                        onAddSnippet = { viewModel.addSnippet(it) },
                        onDeleteSnippet = { viewModel.deleteSnippet(it) }
                    )
                }
                ActiveSheet.CLIPBOARD -> {
                    ClipboardSheet(
                        items = clipboardItems,
                        onInsertText = { text ->
                            viewModel.handleKeyAction(KeyAction.InsertText(text))
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                activeSheet = ActiveSheet.NONE
                            }
                        },
                        onTogglePin = { viewModel.togglePinClipboard(it) },
                        onDeleteItem = { viewModel.deleteClipboardItem(it) },
                        onClearUnpinned = { viewModel.clearUnpinnedClipboard() }
                    )
                }
                ActiveSheet.SETTINGS -> {
                    KeyboardSettingsScreen(
                        preferences = prefs,
                        onUpdatePreferences = { viewModel.updatePreferences(it) }
                    )
                }
                ActiveSheet.NONE -> {}
            }
        }
    }
}
