package com.simonfx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your app's typography
private val AppTypography = Typography()

// Define Light Theme colors
private val LightColors = lightColorScheme(
  primary = Color(0xFF0052CC),
  secondary = Color(0xFF00A676),
  error = Color(0xFFD32F2F),
  background = Color.White,
  onBackground = Color.Black
)

// Define Dark Theme colors
private val DarkColors = darkColorScheme(
  primary = Color(0xFF0052CC),
  secondary = Color(0xFF00A676),
  error = Color(0xFFD32F2F),
  background = Color.Black,
  onBackground = Color.White
)

@Composable
fun SimonFXTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) DarkColors else LightColors

  MaterialTheme(
    colorScheme = colors,
    typography = AppTypography,
    content = content
  )
}
