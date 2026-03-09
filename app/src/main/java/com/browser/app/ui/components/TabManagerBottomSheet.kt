package com.browser.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browser.app.model.Tab
import com.browser.app.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabManagerBottomSheet(
    viewModel: BrowserViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val tabs by viewModel.tabs.collectAsState()
    val currentTabIndex by viewModel.currentTabIndex.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tabs (${tabs.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = {
                    viewModel.addNewTab()
                    onDismiss()
                }) {
                    Text(
                        text = "✕",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            val columns = 4
            val circleSize = 80.dp

            val totalItems = tabs.size + 1
            val rows = (totalItems + columns - 1) / columns

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(columns) { colIndex ->
                            val index = rowIndex * columns + colIndex
                            if (index < totalItems) {
                                if (index < tabs.size) {
                                    val tab = tabs[index]
                                    TabCircleItem(
                                        tab = tab,
                                        isActive = tab.id == tabs.getOrNull(currentTabIndex)?.id,
                                        circleSize = circleSize,
                                        onClick = {
                                            viewModel.switchTab(tab.id)
                                            onDismiss()
                                        },
                                        onClose = {
                                            viewModel.closeTab(tab.id)
                                        }
                                    )
                                } else {
                                    AddTabButton(
                                        circleSize = circleSize,
                                        onClick = {
                                            viewModel.addNewTab()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Swipe down to close",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TabCircleItem(
    tab: com.browser.app.model.Tab,
    isActive: Boolean,
    circleSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isActive) 4.dp else 0.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val favicon = tab.favicon
            if (favicon != null) {
                androidx.compose.foundation.Image(
                    bitmap = favicon.asImageBitmap(),
                    contentDescription = tab.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = tab.title.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onError,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = tab.title.take(12),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AddTabButton(
    circleSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Light
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "New",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
