@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.ivor.ivormusic.ui.theme

import androidx.compose.material3.Typography as MaterialTypography
import com.ivor.ivormusic.R
import androidx.annotation.StringRes
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.R as GoogleFontsR
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Material 3 Expressive Typography using Roboto Flex Variable Font
// Using FontVariation.width() for wider/condensed text styles

// Google Font Provider
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = GoogleFontsR.array.com_google_android_gms_fonts_certs
)

val RobotoFlexFont = GoogleFont("Roboto Flex")

// Standard width (100f) - for body text and regular content
val RobotoFlex = FontFamily(
    Font(
        googleFont = RobotoFlexFont,
        fontProvider = provider,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.width(100f),
            FontVariation.weight(400)
        )
    )
)

val MontserratFont = GoogleFont("Montserrat")

val MontserratFamily = FontFamily(
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Black),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Light),
)

// Medium width (110f) - for section headers
val RobotoFlexMediumWidth = FontFamily(
    Font(
        googleFont = RobotoFlexFont,
        fontProvider = provider,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.width(110f),
            FontVariation.weight(600)
        )
    )
)

val Typography = MaterialTypography(
    displayLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 68.sp,
        textGeometricTransform = TextGeometricTransform(scaleX = 1.5f), // Stretch horizontally
        letterSpacing = (-0.05).em,
        lineHeight = 0.8.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFlexMediumWidth,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/** Persisted visual choices. Keep storage IDs stable across releases. */
enum class AppSurfaceStyle(val storageId: String, @param:StringRes val labelRes: Int) {
    EXPRESSIVE("expressive", R.string.settings_style_expressive),
    LIQUID_GLASS("liquid_glass", R.string.settings_style_liquid_glass);

    companion object {
        fun fromStorageId(value: String?): AppSurfaceStyle =
            entries.firstOrNull { it.storageId == value } ?: EXPRESSIVE
    }
}

enum class AppFontFamily(val storageId: String, @param:StringRes val labelRes: Int) {
    DEFAULT("default", R.string.font_family_default),
    MONTSERRAT("montserrat", R.string.font_family_montserrat),
    SERIF("serif", R.string.font_family_serif),
    MONOSPACE("monospace", R.string.font_family_monospace);

    companion object {
        fun fromStorageId(value: String?): AppFontFamily =
            entries.firstOrNull { it.storageId == value } ?: DEFAULT
    }
}

/** Keep the existing expressive scale and weights while changing only typefaces. */
fun typographyFor(choice: AppFontFamily): MaterialTypography {
    // DEFAULT is the platform sans-serif, not the app's decorative display
    // face. Giving it an explicit family makes switching away from Montserrat
    // visibly change every text role, including body and labels.
    val family = when (choice) {
        AppFontFamily.DEFAULT -> FontFamily.SansSerif
        AppFontFamily.MONTSERRAT -> MontserratFamily
        AppFontFamily.SERIF -> FontFamily.Serif
        AppFontFamily.MONOSPACE -> FontFamily.Monospace
    }
    return MaterialTypography(
        displayLarge = Typography.displayLarge.copy(fontFamily = family),
        displayMedium = Typography.displayMedium.copy(fontFamily = family),
        displaySmall = Typography.displaySmall.copy(fontFamily = family),
        headlineLarge = Typography.headlineLarge.copy(fontFamily = family),
        headlineMedium = Typography.headlineMedium.copy(fontFamily = family),
        headlineSmall = Typography.headlineSmall.copy(fontFamily = family),
        titleLarge = Typography.titleLarge.copy(fontFamily = family),
        titleMedium = Typography.titleMedium.copy(fontFamily = family),
        titleSmall = Typography.titleSmall.copy(fontFamily = family),
        bodyLarge = Typography.bodyLarge.copy(fontFamily = family),
        bodyMedium = Typography.bodyMedium.copy(fontFamily = family),
        bodySmall = Typography.bodySmall.copy(fontFamily = family),
        labelLarge = Typography.labelLarge.copy(fontFamily = family),
        labelMedium = Typography.labelMedium.copy(fontFamily = family),
        labelSmall = Typography.labelSmall.copy(fontFamily = family),
    )
}

