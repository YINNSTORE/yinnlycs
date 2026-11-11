package com.ivor.ivormusic.util

import android.app.Activity

/**
 * Requests the fastest display mode for this app window.
 * Android and the device manufacturer retain the final say (battery saver,
 * thermal limits and user display settings can still cap the refresh rate).
 */
object DisplayPerformance {
    fun maximumRefreshRate(activity: Activity): Float =
        activity.windowManager.defaultDisplay.supportedModes
            .maxOfOrNull { it.refreshRate }
            ?: activity.windowManager.defaultDisplay.refreshRate

    fun apply(activity: Activity, enabled: Boolean): Float {
        val display = activity.windowManager.defaultDisplay
        val preferred = if (enabled) display.supportedModes.maxByOrNull { it.refreshRate } else null
        val attributes = activity.window.attributes
        attributes.preferredDisplayModeId = preferred?.modeId ?: 0
        activity.window.attributes = attributes
        return preferred?.refreshRate ?: display.refreshRate
    }
}
