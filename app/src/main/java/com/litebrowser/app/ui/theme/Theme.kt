package com.litebrowser.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val White          = Color(0xFFFFFFFF)
val Grey50         = Color(0xFFF8F9FA)
val Grey100        = Color(0xFFF1F3F4)
val Grey200        = Color(0xFFE8EAED)
val Grey400        = Color(0xFFBDC1C6)
val Grey600        = Color(0xFF80868B)
val Grey700        = Color(0xFF5F6368)
val Grey900        = Color(0xFF202124)
val Blue600        = Color(0xFF1A73E8)
val Blue50         = Color(0xFFE8F0FE)
val Green600       = Color(0xFF188038)
val Red500         = Color(0xFFEA4335)

private val LightColors = lightColorScheme(
    primary          = Blue600,
    background       = White,
    surface          = White,
    onPrimary        = White,
    onBackground     = Grey900,
    onSurface        = Grey900,
    outline          = Grey200,
)

@Composable
fun BrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
