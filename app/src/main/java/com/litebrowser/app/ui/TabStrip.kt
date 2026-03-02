package com.litebrowser.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
        shadowElevation = 4.dp,
        color = Grey50
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = tabs,
                key = { it.id }
            ) { tab ->
                val isActive = tab.id == activeTabId
                
                val backgroundColor by animateColorAsState(
                    targetValue = if (isActive) White else Grey100,
                    animationSpec = tween(200),
                    label = "background"
                )
                
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )
                
                Card(
                    modifier = Modifier
                        .width(if (isActive) 160.dp else 140.dp)
                        .height(48.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isActive) 4.dp else 1.dp,
                        pressedElevation = 8.dp
                    ),
                    onClick = { onTabClick(tab.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Animated favicon
                        AnimatedContent(
                            targetState = tab.url.startsWith("http"),
                            transitionSpec = {
                                scaleIn() + fadeIn() with scaleOut() + fadeOut()
                            },
                            label = "favicon"
                        ) { isWeb ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isWeb) Blue50 else Grey200
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isWeb) "🌐" else "✦",
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        // Title with animation
                        Text(
                            text = tab.title.ifEmpty { "New Tab" },
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                            color = if (isActive) Grey900 else Grey600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Close button with animation
                        AnimatedVisibility(
                            visible = tabs.size > 1,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            IconButton(
                                onClick = { onTabClose(tab.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(18.dp),
                                    tint = Grey500
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // New Tab FAB
                FloatingActionButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    containerColor = Blue600,
                    contentColor = White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New tab",
                        modifier = Modifier.size(24.dp)
                    )
                }
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
