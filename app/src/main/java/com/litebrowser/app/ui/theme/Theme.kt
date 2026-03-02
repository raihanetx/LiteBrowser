package com.litebrowser.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pure Black & White Theme
val White          = Color(0xFFFFFFFF)
val Grey50         = Color(0xFFF5F5F5)
val Grey100        = Color(0xFFE0E0E0)
val Grey200        = Color(0xFFBDBDBD)
val Grey300        = Color(0xFF9E9E9E)
val Grey400        = Color(0xFF757575)
val Grey500        = Color(0xFF616161)
val Grey600        = Color(0xFF424242)
val Grey700        = Color(0xFF303030)
val Grey800        = Color(0xFF212121)
val Black          = Color(0xFF000000)

private val LightColors = lightColorScheme(
    primary          = Black,
    background       = White,
    surface          = White,
    onPrimary        = White,
    onBackground     = Black,
    onSurface        = Black,
    outline          = Grey300,
)

@Composable
fun BrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
