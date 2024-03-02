package icu.twtool.chat.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import icu.twtool.chat.constants.VIVO

actual val supportedDynamicColor: Boolean
    get() = Build.MANUFACTURER != VIVO

@Composable
actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(context)
    else dynamicLightColorScheme(context)
}