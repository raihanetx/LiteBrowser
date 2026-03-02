package com.litebrowser.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Handle indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Grey300)
                    )
                }
                
                Text(
                    "Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Grey900,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // Zoom Control Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Grey50),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Zoom Level",
                            fontSize = 14.sp,
                            color = Grey600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
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
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Grey200,
                                    disabledContainerColor = Grey100
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onZoomReset() }
                            ) {
                                Text(
                                    "$currentZoom%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentZoom != 100) Blue600 else Grey900,
                                )
                                if (currentZoom != 100) {
                                    Text(
                                        "Tap to reset",
                                        fontSize = 11.sp,
                                        color = Blue600
                                    )
                                }
                            }

                            // Zoom In
                            FilledIconButton(
                                onClick = onZoomIn,
                                enabled = !zoomAtMax,
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Grey200,
                                    disabledContainerColor = Grey100
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
                }

                // Desktop Mode Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Grey50),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onToggleDesktop() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "Desktop Site",
                                fontSize = 16.sp,
                                color = Grey900,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (activeTab?.isDesktopMode == true) 
                                    "Viewing desktop version" 
                                else 
                                    "Viewing mobile version",
                                fontSize = 13.sp,
                                color = Grey600,
                            )
                        }

                        Switch(
                            checked = activeTab?.isDesktopMode ?: false,
                            onCheckedChange = { onToggleDesktop() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue600,
                                uncheckedThumbColor = White,
                                uncheckedTrackColor = Grey300
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tips
                Text(
                    "💡 Tip: Swipe left/right on page to go back/forward",
                    fontSize = 12.sp,
                    color = Grey500,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
