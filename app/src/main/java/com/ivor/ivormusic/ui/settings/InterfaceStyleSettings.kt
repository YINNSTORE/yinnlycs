package com.ivor.ivormusic.ui.settings

import androidx.compose.ui.unit.dp

import androidx.compose.ui.Modifier

import androidx.compose.material3.Text

import androidx.compose.material3.Surface

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ivor.ivormusic.R
import com.ivor.ivormusic.data.ThemePreferences
import com.ivor.ivormusic.ui.theme.AppFontFamily
import com.ivor.ivormusic.ui.theme.AppSurfaceStyle
import com.ivor.ivormusic.ui.theme.typographyFor

@Composable
internal fun InterfaceStyleSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { ThemePreferences(context) }
    val surfaceStyle by preferences.surfaceStyle.collectAsState()
    val fontFamily by preferences.fontFamily.collectAsState()
    // The preview is bound directly to the selected family as well as the
    // app-wide Material theme, so a font choice is immediately visible here.
    val selectedTypography = remember(fontFamily) { typographyFor(fontFamily) }

    SettingsDetailScaffold(
        title = stringResource(R.string.settings_interface_style_title),
        onBack = onBack
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_visual_style)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = stringResource(R.string.settings_visual_style),
                        subtitle = stringResource(
                            if (surfaceStyle == AppSurfaceStyle.LIQUID_GLASS) {
                                R.string.settings_style_liquid_glass_description
                            } else {
                                R.string.settings_visual_style_description
                            }
                        ),
                        labels = listOf(
                            stringResource(R.string.settings_style_expressive),
                            stringResource(R.string.settings_style_liquid_glass)
                        ),
                        selectedIndex = if (surfaceStyle == AppSurfaceStyle.LIQUID_GLASS) 1 else 0,
                        onSelect = { index ->
                            preferences.setSurfaceStyle(
                                if (index == 1) AppSurfaceStyle.LIQUID_GLASS
                                else AppSurfaceStyle.EXPRESSIVE
                            )
                        }
                    )
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_font_family)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.FormatSize,
                        title = stringResource(R.string.settings_font_family),
                        subtitle = stringResource(R.string.settings_font_family_description),
                        labels = listOf(
                            stringResource(R.string.font_family_default),
                            stringResource(R.string.font_family_montserrat),
                            stringResource(R.string.font_family_serif),
                            stringResource(R.string.font_family_monospace)
                        ),
                        selectedIndex = AppFontFamily.entries.indexOf(fontFamily).coerceAtLeast(0),
                        onSelect = { index ->
                            preferences.setFontFamily(AppFontFamily.entries.getOrElse(index) {
                                AppFontFamily.DEFAULT
                            })
                        }
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_style_preview)) {
                SettingsCard {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text("Yinnlycs", style = selectedTypography.headlineSmall)
                            Text(
                                stringResource(R.string.home_greeting_subtitle),
                                style = selectedTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
