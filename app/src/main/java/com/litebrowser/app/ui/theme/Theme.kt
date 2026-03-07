package com.litebrowser.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// EVANLY Zinc Color Palette
val Zinc50 = Color(0xFFFAFAFA)
val Zinc100 = Color(0xFFF4F4F5)
val Zinc200 = Color(0xFFE4E4E7)
val Zinc300 = Color(0xFFD4D4D8)
val Zinc400 = Color(0xFFA1A1AA)
val Zinc500 = Color(0xFF71717A)
val Zinc600 = Color(0xFF52525B)
val Zinc700 = Color(0xFF3F3F46)
val Zinc800 = Color(0xFF27272A)
val Zinc900 = Color(0xFF18181B)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

private val LightColors = lightColorScheme(
    primary = Zinc900,
    background = White,
    surface = White,
    onPrimary = White,
    onBackground = Zinc900,
    onSurface = Zinc900,
    outline = Zinc200,
)

@Composable
fun BrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
