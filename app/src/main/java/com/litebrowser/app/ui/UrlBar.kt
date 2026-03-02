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
            .height(44.dp)
            .background(Grey50)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(enabled = activeTab?.canGoBack == true) { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "‹",
                fontSize = 20.sp,
                color = if (activeTab?.canGoBack == true) Grey700 else Grey400,
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(enabled = activeTab?.canGoForward == true) { onForward() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "›",
                fontSize = 20.sp,
                color = if (activeTab?.canGoForward == true) Grey700 else Grey400,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .background(White, RoundedCornerShape(16.dp))
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) Blue600 else Grey200,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 10.dp)
                .clickable {
                    inputText = activeTab?.url ?: ""
                    isFocused = true
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("🔒", fontSize = 10.sp)

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
                        fontSize = 12.sp,
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
                Text(
                    text = activeTab?.url?.ifEmpty { "Search or type URL" } ?: "Search or type URL",
                    fontSize = 12.sp,
                    color = if (activeTab?.url.isNullOrEmpty()) Grey400 else Grey700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            if (activeTab?.isDesktopMode == true) {
                Box(
                    modifier = Modifier
                        .background(Blue50, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        "DESKTOP",
                        fontSize = 8.sp,
                        color = Blue600,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center,
        ) {
            Text("↻", fontSize = 14.sp, color = Grey700)
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable { onMenuOpen() },
            contentAlignment = Alignment.Center,
        ) {
            Text("⋮", fontSize = 18.sp, color = Grey700)
        }
    }
}
