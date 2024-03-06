package icu.twtool.chat.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import icu.twtool.chat.navigation.window.systemBarWindowInsets
import icu.twtool.chat.theme.ElevationTokens
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_back
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topAppBarColors(): TopAppBarColors {
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
    return TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(
    onBack: () -> Unit,
    title: String? = null,
    windowInsets: WindowInsets = systemBarWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onBack) {
                Icon(painterResource(Res.drawable.ic_back), "返回")
            }
        },
        title = {
            if (title != null) {
                Text(title)
            }
        },
        actions = actions,
        windowInsets = windowInsets,
        colors = topAppBarColors()
    )
}