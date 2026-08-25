package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.KeyboardThemeConfig

@Composable
fun EmojiKeyboardPicker(
    theme: KeyboardThemeConfig,
    recentEmojis: List<String>,
    onEmojiSelect: (String) -> Unit,
    onBackspace: () -> Unit,
    onSwitchToAbc: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val isSearching = searchQuery.isNotBlank()
    val searchResults = remember(searchQuery) {
        if (isSearching) EmojiData.searchEmojis(searchQuery) else emptyList()
    }

    val currentEmojis = remember(selectedCategoryIndex, isSearching, searchResults, recentEmojis) {
        if (isSearching) {
            searchResults
        } else if (selectedCategoryIndex == 0) {
            recentEmojis.ifEmpty { EmojiData.categories[0].emojis.take(24) }
        } else {
            EmojiData.categories[selectedCategoryIndex - 1].emojis
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("emoji_keyboard_picker"),
        color = Color(theme.backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search emojis...", fontSize = 13.sp, color = Color(theme.symbolHintColor)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(theme.symbolHintColor),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(theme.symbolHintColor),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(theme.surfaceColor),
                        unfocusedContainerColor = Color(theme.surfaceColor),
                        focusedBorderColor = Color(theme.accentColor),
                        unfocusedBorderColor = Color(theme.borderStrokeColor),
                        focusedTextColor = Color(theme.keyTextColor),
                        unfocusedTextColor = Color(theme.keyTextColor)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                )

                // Back to ABC quick button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(theme.specialKeyColor))
                        .clickable { onSwitchToAbc() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABC",
                        color = Color(theme.specialKeyTextColor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Backspace button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(theme.specialKeyColor))
                        .clickable { onBackspace() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = Color(theme.specialKeyTextColor),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Category Tabs Bar
            if (!isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(theme.surfaceColor))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recents icon tab
                    val isRecentsSelected = selectedCategoryIndex == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isRecentsSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedCategoryIndex = 0 }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recents",
                            tint = if (isRecentsSelected) Color(theme.accentColor) else Color(theme.symbolHintColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Other Category Tabs
                    EmojiData.categories.forEachIndexed { index, cat ->
                        val isSelected = selectedCategoryIndex == index + 1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { selectedCategoryIndex = index + 1 }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.icon,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // 3. Emoji Grid
            if (currentEmojis.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearching) "No emojis found" else "No recent emojis",
                        color = Color(theme.symbolHintColor),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 42.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(currentEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEmojiSelect(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
