package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*

@Composable
fun TabStrip(
    tabs:        List<BrowserTab>,
    activeTabId: String?,
    onTabClick:  (String) -> Unit,
    onTabClose:  (String) -> Unit,
    onNewTab:    () -> Unit,
    modifier:    Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Grey50)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Bottom,
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId

            Row(
                modifier = Modifier
                    .height(if (isActive) 36.dp else 32.dp)
                    .widthIn(min = 90.dp, max = 140.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(if (isActive) White else Color.Transparent)
                    .drawBehind {
                        if (isActive) {
                            drawLine(
                                color = Color(0xFF1A73E8),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                    }
                    .clickable { onTabClick(tab.id) }
                    .padding(start = 12.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (tab.url.isEmpty()) "✦" else "🌐",
                    fontSize = 11.sp,
                )

                Text(
                    text = tab.title.ifEmpty { "New Tab" },
                    fontSize = 11.sp,
                    color = if (isActive) Grey900 else Grey700,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (tabs.size > 1) {
                    Text(
                        text = "×",
                        fontSize = 14.sp,
                        color = Grey600,
                        modifier = Modifier
                            .size(18.dp)
                            .wrapContentSize()
                            .clickable { onTabClose(tab.id) },
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(Grey200)
                    .align(Alignment.Bottom)
            )
        }

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(32.dp)
                .align(Alignment.Bottom)
                .clickable { onNewTab() },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 18.sp, color = Grey700)
        }

        Spacer(modifier = Modifier.weight(1f).height(40.dp).background(Grey50))
    }
}
