package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*

@Composable
fun BrowserMenu(
    expanded:         Boolean,
    activeTab:        BrowserTab?,
    onDismiss:        () -> Unit,
    onZoomIn:         () -> Unit,
    onZoomOut:        () -> Unit,
    onZoomReset:      () -> Unit,
    onToggleDesktop:  () -> Unit,
) {
    DropdownMenu(
        expanded          = expanded,
        onDismissRequest  = onDismiss,
        offset            = DpOffset(x = (-8).dp, y = 4.dp),
        modifier = Modifier
            .width(260.dp)
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Grey200, RoundedCornerShape(12.dp))
    ) {

        DropdownMenuItem(
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Zoom", fontSize = 14.sp, color = Grey700)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val zoomAtMin = (activeTab?.zoomLevel ?: 100) <= 50
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (zoomAtMin) Grey50 else White,
                                    CircleShape
                                )
                                .border(1.dp, Grey200, CircleShape)
                                .clickable(enabled = !zoomAtMin) { onZoomOut() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "−",
                                fontSize = 18.sp,
                                color = if (zoomAtMin) Grey400 else Grey900,
                            )
                        }

                        val currentZoom = activeTab?.zoomLevel ?: 100
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .clickable { onZoomReset() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$currentZoom%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (currentZoom != 100) Blue600 else Grey900,
                            )
                        }

                        val zoomAtMax = (activeTab?.zoomLevel ?: 100) >= 300
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (zoomAtMax) Grey50 else White,
                                    CircleShape
                                )
                                .border(1.dp, Grey200, CircleShape)
                                .clickable(enabled = !zoomAtMax) { onZoomIn() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "+",
                                fontSize = 18.sp,
                                color = if (zoomAtMax) Grey400 else Grey900,
                            )
                        }
                    }
                }
            },
            onClick = { },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        )

        HorizontalDivider(color = Grey200, thickness = 1.dp)

        val isDesktop = activeTab?.isDesktopMode ?: false
        DropdownMenuItem(
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Desktop site", fontSize = 14.sp, color = Grey900)
                        Text(
                            if (isDesktop) "Showing desktop version"
                            else "Showing mobile version",
                            fontSize = 11.sp,
                            color = Grey600,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(24.dp)
                            .background(
                                if (isDesktop) Blue600 else Grey200,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleDesktop() },
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = if (isDesktop) 23.dp else 3.dp, top = 3.dp)
                                .size(18.dp)
                                .background(White, CircleShape)
                        )
                    }
                }
            },
            onClick = { onToggleDesktop() },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}
