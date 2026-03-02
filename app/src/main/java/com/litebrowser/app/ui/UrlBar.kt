package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*

@Composable
fun UrlBar(
    activeTab:   BrowserTab?,
    onNavigate:  (String) -> Unit,
    onBack:      () -> Unit,
    onForward:   () -> Unit,
    onRefresh:   () -> Unit,
    onMenuOpen:  () -> Unit,
    modifier:    Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(activeTab?.url) {
        if (!isFocused) {
            inputText = activeTab?.url ?: ""
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Grey50)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = activeTab?.canGoBack == true) { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "‹",
                fontSize = 22.sp,
                color = if (activeTab?.canGoBack == true) Grey700 else Grey400,
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = activeTab?.canGoForward == true) { onForward() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "›",
                fontSize = 22.sp,
                color = if (activeTab?.canGoForward == true) Grey700 else Grey400,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(White, RoundedCornerShape(18.dp))
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Blue600 else Grey200,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("🔒", fontSize = 11.sp)

            if (isFocused) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = Grey900,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            onNavigate(inputText)
                            isFocused = false
                        }
                    ),
                )
            } else {
                Text(
                    text = activeTab?.url?.ifEmpty { "Search or type URL" } ?: "Search or type URL",
                    fontSize = 13.sp,
                    color = if (activeTab?.url.isNullOrEmpty()) Grey400 else Grey700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inputText = activeTab?.url ?: ""
                            isFocused = true
                        },
                )
            }

            if (activeTab?.isDesktopMode == true) {
                Box(
                    modifier = Modifier
                        .background(Blue50, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "DESKTOP",
                        fontSize = 9.sp,
                        color = Blue600,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center,
        ) {
            Text("↻", fontSize = 16.sp, color = Grey700)
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable { onMenuOpen() },
            contentAlignment = Alignment.Center,
        ) {
            Text("⋮", fontSize = 20.sp, color = Grey700)
        }
    }
}
