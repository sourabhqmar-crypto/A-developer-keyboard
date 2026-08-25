package com.example.ui.keyboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.KeyboardThemeConfig

@Composable
fun TextEditingPad(
    theme: KeyboardThemeConfig,
    onAction: (KeyAction) -> Unit,
    onSwitchToAbc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("text_editing_pad"),
        color = Color(theme.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Editing & Cursor Control",
                    color = Color(theme.keyTextColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(theme.accentColor))
                        .clickable { onSwitchToAbc() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABC",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action Tool Strip (Select All, Cut, Copy, Paste, Undo, Redo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EditToolButton(label = "Select All", icon = Icons.Default.SelectAll, theme = theme, modifier = Modifier.weight(1f)) {
                    onAction(KeyAction.SelectAll)
                }
                EditToolButton(label = "Cut", icon = Icons.Default.ContentCut, theme = theme, modifier = Modifier.weight(1f)) {
                    onAction(KeyAction.Cut)
                }
                EditToolButton(label = "Copy", icon = Icons.Default.ContentCopy, theme = theme, modifier = Modifier.weight(1f)) {
                    onAction(KeyAction.Copy)
                }
                EditToolButton(label = "Paste", icon = Icons.Default.ContentPaste, theme = theme, modifier = Modifier.weight(1f)) {
                    onAction(KeyAction.Paste)
                }
            }

            // D-Pad Navigation Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Quick Jump (Home / Start)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditToolButton(label = "Start", icon = Icons.Default.FirstPage, theme = theme, modifier = Modifier.fillMaxSize().weight(1f)) {
                        onAction(KeyAction.CursorHome)
                    }
                    EditToolButton(label = "Undo", icon = Icons.Default.Undo, theme = theme, modifier = Modifier.fillMaxSize().weight(1f)) {
                        onAction(KeyAction.Undo)
                    }
                }

                // Center 4-Way D-Pad
                Column(
                    modifier = Modifier.weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Up Arrow
                    Box(
                        modifier = Modifier
                            .size(52.dp, 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(theme.keyColor))
                            .clickable { onAction(KeyAction.CursorMove(0, -1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color(theme.keyTextColor))
                    }

                    // Left & Right Arrows
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp, 44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(theme.keyColor))
                                .clickable { onAction(KeyAction.CursorMove(-1, 0)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Left", tint = Color(theme.keyTextColor))
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp, 44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(theme.keyColor))
                                .clickable { onAction(KeyAction.CursorMove(1, 0)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right", tint = Color(theme.keyTextColor))
                        }
                    }

                    // Down Arrow
                    Box(
                        modifier = Modifier
                            .size(52.dp, 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(theme.keyColor))
                            .clickable { onAction(KeyAction.CursorMove(0, 1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color(theme.keyTextColor))
                    }
                }

                // Right Quick Jump (End / Redo)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditToolButton(label = "End", icon = Icons.Default.LastPage, theme = theme, modifier = Modifier.fillMaxSize().weight(1f)) {
                        onAction(KeyAction.CursorEnd)
                    }
                    EditToolButton(label = "Redo", icon = Icons.Default.Redo, theme = theme, modifier = Modifier.fillMaxSize().weight(1f)) {
                        onAction(KeyAction.Redo)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditToolButton(
    label: String,
    icon: ImageVector,
    theme: KeyboardThemeConfig,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(theme.surfaceColor))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(theme.accentColor),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color(theme.keyTextColor),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
