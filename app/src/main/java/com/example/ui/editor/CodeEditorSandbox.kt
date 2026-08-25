package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeFile
import com.example.data.model.KeyboardThemeConfig
import com.example.data.repository.KeyboardPreferences
import com.example.ui.theme.KeyboardThemes
import kotlinx.coroutines.launch

@Composable
fun CodeEditorSandbox(
    files: List<CodeFile>,
    activeFile: CodeFile?,
    onSelectFile: (CodeFile) -> Unit,
    onSaveFile: (CodeFile) -> Unit,
    onDeleteFile: (CodeFile) -> Unit,
    onCreateFile: (String, String) -> Unit,
    preferences: KeyboardPreferences,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val theme = remember(preferences.themeId) { KeyboardThemes.getThemeById(preferences.themeId) }
    val runnerEngine = remember { CodeRunnerEngine(context) }

    var isRunning by remember { mutableStateOf(false) }
    var executionResult by remember { mutableStateOf<ExecutionResult?>(null) }
    var showTerminal by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }

    // Line & Column calculation
    val lines = textFieldValue.text.lines()
    val cursorIndex = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
    var currentLine = 1
    var currentCol = 1
    var count = 0
    for ((index, line) in lines.withIndex()) {
        val nextCount = count + line.length + 1
        if (cursorIndex <= nextCount) {
            currentLine = index + 1
            currentCol = (cursorIndex - count) + 1
            break
        }
        count = nextCount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(theme.backgroundColor))
    ) {
        // 1. File Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(theme.surfaceColor))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            files.forEach { file ->
                val isActive = file.id == activeFile?.id || (activeFile == null && file == files.firstOrNull())
                val tabBg = if (isActive) Color(theme.backgroundColor) else Color(theme.surfaceColor)
                val tabText = if (isActive) Color(theme.keyTextColor) else Color(theme.symbolHintColor)
                val borderCol = if (isActive) Color(theme.accentColor) else Color.Transparent

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(tabBg)
                        .border(1.dp, borderCol, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .clickable { onSelectFile(file) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val langIcon = when (file.language.lowercase()) {
                        "python" -> "🐍"
                        "javascript" -> "⚡"
                        "kotlin" -> "🟣"
                        "html" -> "🌐"
                        "sql" -> "🗄️"
                        "c++" -> "⚙️"
                        else -> "📄"
                    }
                    Text(text = langIcon, fontSize = 11.sp)
                    Text(
                        text = file.name,
                        color = tabText,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // New File Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(theme.keyColor))
                    .clickable { showNewFileDialog = true }
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New File",
                    tint = Color(theme.keyTextColor),
                    modifier = Modifier.width(16.dp).height(16.dp)
                )
            }
        }

        // 2. Toolbar & Status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(theme.backgroundColor))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Stats (Ln, Col, Char count)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Ln $currentLine, Col $currentCol",
                    color = Color(theme.accentColor),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${textFieldValue.text.length} chars",
                    color = Color(theme.symbolHintColor),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${lines.size} lines",
                    color = Color(theme.symbolHintColor),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Save Button
                IconButton(
                    onClick = {
                        activeFile?.let {
                            onSaveFile(it.copy(content = textFieldValue.text))
                        }
                    },
                    modifier = Modifier.width(32.dp).height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save File",
                        tint = Color(theme.keyTextColor),
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                }

                // Copy All
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(textFieldValue.text))
                    },
                    modifier = Modifier.width(32.dp).height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy All",
                        tint = Color(theme.keyTextColor),
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                }

                // Toggle Terminal
                IconButton(
                    onClick = { showTerminal = !showTerminal },
                    modifier = Modifier.width(32.dp).height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Toggle Terminal",
                        tint = if (showTerminal) Color(theme.accentColor) else Color(theme.keyTextColor),
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                }

                // Run Code
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isRunning = true
                            showTerminal = true
                            val lang = activeFile?.language ?: "JavaScript"
                            executionResult = runnerEngine.execute(textFieldValue.text, lang)
                            isRunning = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(theme.accentColor),
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.width(14.dp).height(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                modifier = Modifier.width(14.dp).height(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(theme.borderStrokeColor), thickness = 1.dp)

        // 3. Editor Gutter and Text Canvas
        val editorScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .background(Color(theme.surfaceColor))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lines.size.coerceAtLeast(1)) {
                        Text(
                            text = "$i",
                            color = if (i == currentLine) Color(theme.accentColor) else Color(theme.symbolHintColor).copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = if (i == currentLine) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Highlighted Code Editor TextField
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = onTextFieldValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("code_editor_input"),
                        textStyle = TextStyle(
                            color = Color(theme.keyTextColor),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Color(theme.accentColor)),
                        visualTransformation = { text ->
                            val annotated = SyntaxHighlighter.highlight(text.text, theme)
                            androidx.compose.ui.text.input.TransformedText(
                                annotated,
                                androidx.compose.ui.text.input.OffsetMapping.Identity
                            )
                        }
                    )
                }
            }
        }

        // 4. Interactive Terminal Console Output Panel
        AnimatedVisibility(visible = showTerminal) {
            TerminalConsoleView(
                result = executionResult,
                isRunning = isRunning,
                theme = theme,
                onClose = { showTerminal = false },
                onClear = { executionResult = null }
            )
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        var selectedLang by remember { mutableStateOf("Python") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New Code File", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name (e.g. main.py, query.sql)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Language:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Python", "JavaScript", "Kotlin", "C++", "HTML", "SQL", "Bash").forEach { lang ->
                            val isSel = selectedLang == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedLang = lang }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lang,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFileName.ifBlank { "snippet_${System.currentTimeMillis() % 1000}.${selectedLang.take(2).lowercase()}" }
                        onCreateFile(name, selectedLang)
                        showNewFileDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TerminalConsoleView(
    result: ExecutionResult?,
    isRunning: Boolean,
    theme: KeyboardThemeConfig,
    onClose: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            // Terminal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💻 TERMINAL OUTPUT", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    if (result != null) {
                        Text("(${result.executionTimeMs}ms)", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClear, modifier = Modifier.width(24.dp).height(24.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.width(14.dp).height(14.dp))
                    }
                    IconButton(onClick = onClose, modifier = Modifier.width(24.dp).height(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.width(14.dp).height(14.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

            // Terminal Logs List
            if (isRunning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.width(16.dp).height(16.dp), strokeWidth = 2.dp)
                        Text("Executing script...", color = Color(0xFF94A3B8), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            } else if (result == null || result.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Press RUN to execute code and see console output.", color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(result.logs) { log ->
                        val logColor = when {
                            log.startsWith("ERROR") || log.startsWith("🛑") -> Color(0xFFF43F5E)
                            log.startsWith(">") || log.startsWith("$") -> Color(0xFFFBBF24)
                            log.startsWith("Return:") -> Color(0xFFA855F7)
                            else -> Color(0xFF34D399)
                        }
                        Text(
                            text = log,
                            color = logColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
