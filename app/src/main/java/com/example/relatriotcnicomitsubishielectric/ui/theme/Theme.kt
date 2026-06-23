package com.example.relatriotcnicomitsubishielectric.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary          = MitsubishiRed,
    onPrimary        = CardWhite,
    primaryContainer = MitsubishiRedDark,
    secondary        = Steel,
    onSecondary      = CardWhite,
    background       = Paper,
    onBackground     = InkDark,
    surface          = CardWhite,
    onSurface        = InkDark,
    outline          = LineGray,
    error            = MitsubishiRed
)

@Composable
fun RatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content
    )
}
