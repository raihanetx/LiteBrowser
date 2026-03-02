package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Grey50)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = activeTab?.canGoBack == true) { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "←",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (activeTab?.canGoBack == true) Grey700 else Grey400,
            )
        }

        // Forward Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = activeTab?.canGoForward == true) { onForward() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "→",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (activeTab?.canGoForward == true) Grey700 else Grey400,
            )
        }

        // URL Field
        Row(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(White, RoundedCornerShape(18.dp))
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) Blue600 else Grey300,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 12.dp)
                .clickable {
                    inputText = activeTab?.url ?: ""
                    isFocused = true
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Security Icon
            val isSecure = activeTab?.url?.startsWith("https://") == true
            Text(
                text = if (isSecure) "🔒" else "⚠️",
                fontSize = 12.sp,
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
                        fontSize = 13.sp,
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
                    activeTab.url.startsWith("https://lite.duckduckgo.com") -> "DuckDuckGo Lite Search"
                    else -> activeTab.url.replace("https://", "").replace("http://", "").substringBefore("/")
                }
                
                Text(
                    text = displayText,
                    fontSize = 13.sp,
                    color = if (activeTab?.url.isNullOrEmpty() || activeTab?.url?.startsWith("about:") == true) Grey500 else Grey800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            // Desktop Mode Badge
            if (activeTab?.isDesktopMode == true) {
                Box(
                    modifier = Modifier
                        .background(Blue50, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "DESKTOP",
                        fontSize = 9.sp,
                        color = Blue600,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Refresh Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center,
        ) {
            Text("↻", fontSize = 18.sp, color = Grey600)
        }

        // Menu Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable { onMenuOpen() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "⋮",
                fontSize = 20.sp,
                color = Grey600,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
