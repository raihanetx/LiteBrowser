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
    tabs: List<BrowserTab>,
    activeTabId: String?,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Grey50)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Bottom,
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId

            Row(
                modifier = Modifier
                    .height(if (isActive) 34.dp else 30.dp)
                    .widthIn(min = 100.dp, max = 160.dp)
                    .padding(end = if (isActive) 0.dp else 1.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (isActive) White else Grey100)
                    .drawBehind {
                        if (isActive) {
                            drawLine(
                                color = Blue600,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                    .clickable { onTabClick(tab.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (tab.url.isEmpty() || tab.url.startsWith("about")) "✦" else "🌐",
                    fontSize = 11.sp,
                )

                Text(
                    text = tab.title.ifEmpty { "New Tab" },
                    fontSize = 12.sp,
                    color = if (isActive) Grey900 else Grey700,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (tabs.size > 1) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onTabClose(tab.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            color = Grey500,
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNewTab() },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 20.sp, color = Grey600, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}
