package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val ChakraPetchFamily = FontFamily(
  Font(R.font.chakra_petch, FontWeight.Normal),
  Font(R.font.chakra_petch, FontWeight.Medium),
  Font(R.font.chakra_petch, FontWeight.SemiBold),
  Font(R.font.chakra_petch, FontWeight.Bold),
)

val InterFamily = FontFamily(
  Font(R.font.inter, FontWeight.Normal),
  Font(R.font.inter, FontWeight.Medium),
  Font(R.font.inter, FontWeight.SemiBold),
  Font(R.font.inter, FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.Bold),
)

val VoxForgeTypography =
  Typography(
    displaySmall =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.8.sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp,
      ),
    headlineSmall =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.4.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
      ),
    titleSmall =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.6.sp,
      ),
    labelMedium =
      TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp,
      ),
  )
