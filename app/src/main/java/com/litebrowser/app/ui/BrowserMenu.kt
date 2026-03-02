package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp, top = 8.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Grey300)
                    )
                }
                
                // Title
                Text(
                    "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Zoom Control
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Grey50),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Zoom",
                            fontSize = 14.sp,
                            color = Grey600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
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
                            Button(
                                onClick = onZoomOut,
                                enabled = !zoomAtMin,
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Black,
                                    disabledContainerColor = Grey300
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Zoom out",
                                    tint = White
                                )
                            }

                            // Current Zoom
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onZoomReset() }
                            ) {
                                Text(
                                    "$currentZoom%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentZoom != 100) Black else Grey600,
                                )
                                if (currentZoom != 100) {
                                    Text(
                                        "Reset",
                                        fontSize = 12.sp,
                                        color = Grey500
                                    )
                                }
                            }

                            // Zoom In
                            Button(
                                onClick = onZoomIn,
                                enabled = !zoomAtMax,
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Black,
                                    disabledContainerColor = Grey300
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Zoom in",
                                    tint = White
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
                            .padding(20.dp)
                            .clickable { onToggleDesktop() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "Desktop Mode",
                                fontSize = 16.sp,
                                color = Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (activeTab?.isDesktopMode == true) 
                                    "Enabled" 
                                else 
                                    "Disabled",
                                fontSize = 13.sp,
                                color = Grey600,
                            )
                        }

                        Switch(
                            checked = activeTab?.isDesktopMode ?: false,
                            onCheckedChange = { onToggleDesktop() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Black,
                                uncheckedThumbColor = White,
                                uncheckedTrackColor = Grey400
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tips
                Text(
                    "LitEBrowser - Fast & Lightweight",
                    fontSize = 12.sp,
                    color = Grey400,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
