package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UrlBar(
    activeTab: BrowserTab?,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onMenuOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember(activeTab?.id) { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(activeTab?.url, activeTab?.id) {
        if (!isFocused && activeTab != null) {
            inputText = activeTab.url
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = Grey50
    ) {
        Column {
            // Progress bar (loading indicator)
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = Black,
                    trackColor = Grey200,
                )
            }
            
            // Main controls row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    enabled = activeTab?.canGoBack == true,
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (activeTab?.canGoBack == true) Black else Grey400
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Forward Button
                IconButton(
                    onClick = onForward,
                    enabled = activeTab?.canGoForward == true,
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (activeTab?.canGoForward == true) Black else Grey400
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // URL Field - Center piece
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = White,
                    shadowElevation = if (isFocused) 4.dp else 1.dp,
                    border = if (isFocused) {
                        androidx.compose.foundation.BorderStroke(2.dp, Black)
                    } else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                inputText = activeTab?.url ?: ""
                                isFocused = true
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = Grey500
                        )

                        if (isFocused) {
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        isFocused = it.isFocused
                                        if (!it.isFocused) {
                                            keyboardController?.hide()
                                        }
                                    },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = Black,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        if (inputText.isNotBlank()) {
                                            onNavigate(inputText)
                                            isFocused = false
                                            keyboardController?.hide()
                                        }
                                    }
                                ),
                            )
                        } else {
                            val displayText = when {
                                activeTab?.url.isNullOrEmpty() -> "Search or type URL"
                                activeTab.url.startsWith("about:") -> "Home"
                                activeTab.url.startsWith("https://lite.duckduckgo.com") -> "Search"
                                else -> activeTab.url
                                    .replace("https://", "")
                                    .replace("http://", "")
                                    .substringBefore("/")
                                    .take(28)
                            }
                            
                            Text(
                                text = displayText,
                                fontSize = 15.sp,
                                color = if (activeTab?.url.isNullOrEmpty()) Grey500 else Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        // Desktop badge (small)
                        if (activeTab?.isDesktopMode == true) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Grey200,
                                modifier = Modifier.height(20.dp)
                            ) {
                                Text(
                                    "D",
                                    fontSize = 10.sp,
                                    color = Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(24.dp),
                        tint = Black
                    )
                }

                // Menu Button
                IconButton(
                    onClick = onMenuOpen,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp),
                        tint = Black
                    )
                }
            }
        }
    }
}
