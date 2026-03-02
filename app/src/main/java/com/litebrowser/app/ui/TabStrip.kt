package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabStrip(
    tabs: List<BrowserTab>,
    activeTabId: String?,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tabs list takes most space
            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTabId
                    
                    Surface(
                        modifier = Modifier
                            .widthIn(min = 100.dp, max = 160.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isActive) Black else Grey100,
                        shadowElevation = if (isActive) 2.dp else 0.dp,
                        onClick = { onTabClick(tab.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tab icon
                            Text(
                                text = if (tab.url.startsWith("http")) "E" else "N",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) White else Grey600,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isActive) Grey700 else Grey300)
                                    .wrapContentSize(Alignment.Center)
                            )
                            
                            // Tab title
                            Text(
                                text = tab.title.ifEmpty { "New" },
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                                color = if (isActive) White else Grey800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Close button (only if more than 1 tab)
                            if (tabs.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onTabClose(tab.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isActive) Grey300 else Grey500
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // New Tab button - fixed size, never cut off
            FloatingActionButton(
                onClick = onNewTab,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                containerColor = Black,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New tab",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Auto-scroll to active tab
    LaunchedEffect(activeTabId) {
        val index = tabs.indexOfFirst { it.id == activeTabId }
        if (index >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }
}
