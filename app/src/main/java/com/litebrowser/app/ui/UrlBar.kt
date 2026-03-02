package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
        shadowElevation = 8.dp,
        color = Grey50
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = activeTab?.canGoBack == true,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                    tint = if (activeTab?.canGoBack == true) Grey700 else Grey400
                )
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = activeTab?.canGoForward == true,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward",
                    modifier = Modifier.size(24.dp),
                    tint = if (activeTab?.canGoForward == true) Grey700 else Grey400
                )
            }

            // URL Field - Modern Design
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .clickable {
                            inputText = activeTab?.url ?: ""
                            isFocused = true
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Search/Security Icon
                    Icon(
                        imageVector = if (activeTab?.url?.startsWith("https://") == true) 
                            Icons.Default.Search else Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp),
                        tint = if (activeTab?.url?.startsWith("https://") == true) 
                            Green600 else Grey500
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
                                color = Grey900,
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
                            activeTab?.url.isNullOrEmpty() -> "Search or enter address"
                            activeTab.url.startsWith("about:") -> "Home"
                            activeTab.url.startsWith("https://lite.duckduckgo.com") -> "Search DuckDuckGo"
                            else -> activeTab.url.replace("https://", "").replace("http://", "").take(30)
                        }

                        Text(
                            text = displayText,
                            fontSize = 15.sp,
                            color = if (activeTab?.url.isNullOrEmpty() || activeTab?.url?.startsWith("about:") == true) 
                                Grey500 else Grey800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Desktop badge
                    if (activeTab?.isDesktopMode == true) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Blue100)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Desktop",
                                fontSize = 10.sp,
                                color = Blue700,
                                fontWeight = FontWeight.Medium
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
                    tint = Grey600
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
                    tint = Grey600
                )
            }
        }
    }
}
