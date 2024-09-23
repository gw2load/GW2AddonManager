package com.gw2tb.manager.ui.composables

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.gw2tb.manager.AppInfo

val LocalApplicationInfo: ProvidableCompositionLocal<AppInfo> = staticCompositionLocalOf {
    AppInfo(
        version = "Unknown"
    )
}