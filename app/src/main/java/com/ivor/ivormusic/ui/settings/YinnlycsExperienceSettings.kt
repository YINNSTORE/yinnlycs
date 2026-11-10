package com.ivor.ivormusic.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivor.ivormusic.R
import com.ivor.ivormusic.data.AppLocaleManager
import com.ivor.ivormusic.data.ExperiencePreferences
import com.ivor.ivormusic.service.DynamicIslandService
import com.ivor.ivormusic.service.YinnlycsEqualizer
import com.ivor.ivormusic.util.DisplayPerformance
import kotlin.math.roundToInt

@Composable
internal fun LanguageSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val selected = AppLocaleManager.currentLanguage(context)
    val selectedIndex = when (selected) {
        "id" -> 1
        "en" -> 2
        else -> 0
    }
    SettingsDetailScaffold(title = stringResource(R.string.settings_language), onBack = onBack) {
        item {
            SettingsSection(title = stringResource(R.string.settings_language)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.FormatSize,
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                        labels = listOf(
                            stringResource(R.string.settings_language_system),
                            stringResource(R.string.settings_language_indonesian),
                            stringResource(R.string.settings_language_english)
                        ),
                        selectedIndex = selectedIndex,
                        onSelect = { index ->
                            val language = listOf("system", "id", "en").getOrElse(index) { "system" }
                            AppLocaleManager.setLanguage(context, language)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PerformanceSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val preferences = remember(context) { ExperiencePreferences(context) }
    val enabled by preferences.highRefreshRate.collectAsState()
    val maximum = activity?.let { DisplayPerformance.maximumRefreshRate(it) } ?: 60f

    SettingsDetailScaffold(title = stringResource(R.string.settings_performance), onBack = onBack) {
        item {
            SettingsSection(title = stringResource(R.string.settings_performance)) {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_refresh_rate), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.settings_refresh_rate_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.settings_refresh_rate_device_max, maximum),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                preferences.setHighRefreshRate(checked)
                                activity?.let { DisplayPerformance.apply(it, checked) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DynamicIslandSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { ExperiencePreferences(context) }
    val enabled by preferences.dynamicIslandEnabled.collectAsState()
    val width by preferences.dynamicIslandWidthDp.collectAsState()
    val height by preferences.dynamicIslandHeightDp.collectAsState()
    val theme by preferences.dynamicIslandTheme.collectAsState()
    var permissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, context) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                permissionGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun refreshOverlay() {
        if (enabled && permissionGranted) {
            context.startService(Intent(context, DynamicIslandService::class.java).setAction(DynamicIslandService.ACTION_REFRESH))
        }
    }

    SettingsDetailScaffold(title = stringResource(R.string.settings_dynamic_island), onBack = onBack) {
        item {
            SettingsSection(title = stringResource(R.string.settings_dynamic_island)) {
                SettingsCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_dynamic_island_enable), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.settings_dynamic_island_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enabled,
                                enabled = permissionGranted,
                                onCheckedChange = { checked ->
                                    preferences.setDynamicIslandEnabled(checked)
                                    if (checked) {
                                        context.startService(Intent(context, DynamicIslandService::class.java).setAction(DynamicIslandService.ACTION_SHOW))
                                    } else {
                                        context.stopService(Intent(context, DynamicIslandService::class.java))
                                    }
                                }
                            )
                        }
                        if (!permissionGranted) {
                            Button(onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + context.packageName)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(intent) }
                            }) {
                                Text(stringResource(R.string.settings_dynamic_island_permission))
                            }
                        }
                    }
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_dynamic_island_theme)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = stringResource(R.string.settings_dynamic_island_theme),
                        subtitle = stringResource(R.string.settings_dynamic_island_subtitle),
                        labels = listOf(
                            stringResource(R.string.settings_dynamic_island_theme_glass),
                            stringResource(R.string.settings_dynamic_island_theme_violet),
                            stringResource(R.string.settings_dynamic_island_theme_ocean)
                        ),
                        selectedIndex = listOf("glass", "violet", "ocean").indexOf(theme).coerceAtLeast(0),
                        onSelect = { index ->
                            preferences.setDynamicIslandTheme(listOf("glass", "violet", "ocean").getOrElse(index) { "glass" })
                            refreshOverlay()
                        }
                    )
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_dynamic_island_width)) {
                SettingsCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(stringResource(R.string.settings_dynamic_island_width) + ": " + width + " dp")
                        Slider(
                            value = width.toFloat(),
                            onValueChange = {
                                preferences.setDynamicIslandWidthDp(it.roundToInt())
                                refreshOverlay()
                            },
                            valueRange = 220f..360f,
                            steps = 6
                        )
                        Text(stringResource(R.string.settings_dynamic_island_height) + ": " + height + " dp")
                        Slider(
                            value = height.toFloat(),
                            onValueChange = {
                                preferences.setDynamicIslandHeightDp(it.roundToInt())
                                refreshOverlay()
                            },
                            valueRange = 36f..64f,
                            steps = 6
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EqualizerSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember(context) { mutableStateOf(YinnlycsEqualizer.isEnabled(context)) }
    val levels = remember(context) {
        mutableStateListOf<Int>().apply { addAll(YinnlycsEqualizer.getBandLevels(context)) }
    }
    val labels = listOf(
        R.string.settings_equalizer_band_60,
        R.string.settings_equalizer_band_230,
        R.string.settings_equalizer_band_910,
        R.string.settings_equalizer_band_3600,
        R.string.settings_equalizer_band_14000
    )

    SettingsDetailScaffold(title = stringResource(R.string.settings_equalizer), onBack = onBack) {
        item {
            SettingsSection(title = stringResource(R.string.settings_equalizer)) {
                SettingsCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_equalizer_enabled), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.settings_equalizer_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = {
                                    enabled = it
                                    YinnlycsEqualizer.setEnabled(context, it)
                                }
                            )
                        }
                        labels.forEachIndexed { index, label ->
                            Column {
                                Text(
                                    stringResource(label) + " · " + levels[index] + " dB",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Slider(
                                    value = levels[index].toFloat(),
                                    onValueChange = {
                                        val value = it.roundToInt().coerceIn(-15, 15)
                                        levels[index] = value
                                        YinnlycsEqualizer.setBandLevel(context, index, value)
                                    },
                                    valueRange = -15f..15f,
                                    steps = 29
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.settings_equalizer_session),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { YinnlycsEqualizer.openSystemEqualizer(context) }) {
                            Text(stringResource(R.string.settings_equalizer_system))
                        }
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
