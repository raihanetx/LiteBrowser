package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
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
    expanded: Boolean,
    activeTab: BrowserTab?,
    onDismiss: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit,
    onToggleDesktop: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = (-8).dp, y = (-8).dp),
        modifier = Modifier
            .width(280.dp)
            .background(White, RoundedCornerShape(16.dp))
            .border(1.dp, Grey200, RoundedCornerShape(16.dp))
    ) {
        // Zoom Section
        DropdownMenuItem(
            text = {
                Column {
                    Text(
                        "Zoom Level",
                        fontSize = 12.sp,
                        color = Grey600,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        val zoomAtMin = (activeTab?.zoomLevel ?: 100) <= 50
                        val zoomAtMax = (activeTab?.zoomLevel ?: 100) >= 300
                        val currentZoom = activeTab?.zoomLevel ?: 100

                        // Zoom Out
                        FilledIconButton(
                            onClick = onZoomOut,
                            enabled = !zoomAtMin,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Grey100,
                                disabledContainerColor = Grey50
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom out",
                                tint = if (zoomAtMin) Grey400 else Grey800
                            )
                        }

                        // Current Zoom
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$currentZoom%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentZoom != 100) Blue600 else Grey900,
                            )
                            if (currentZoom != 100) {
                                Text(
                                    "Tap to reset",
                                    fontSize = 10.sp,
                                    color = Blue600,
                                    modifier = Modifier.clickable { onZoomReset() }
                                )
                            }
                        }

                        // Zoom In
                        FilledIconButton(
                            onClick = onZoomIn,
                            enabled = !zoomAtMax,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Grey100,
                                disabledContainerColor = Grey50
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom in",
                                tint = if (zoomAtMax) Grey400 else Grey800
                            )
                        }
                    }
                }
            },
            onClick = { },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        )

        HorizontalDivider(
            color = Grey200,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Desktop Mode Toggle
        val isDesktop = activeTab?.isDesktopMode ?: false
        DropdownMenuItem(
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "Desktop Site",
                            fontSize = 15.sp,
                            color = Grey900,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            if (isDesktop) "Desktop mode enabled" else "Mobile mode enabled",
                            fontSize = 12.sp,
                            color = Grey600,
                        )
                    }

                    Switch(
                        checked = isDesktop,
                        onCheckedChange = { onToggleDesktop() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Blue600,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = Grey300
                        )
                    )
                }
            },
            onClick = { onToggleDesktop() },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}
