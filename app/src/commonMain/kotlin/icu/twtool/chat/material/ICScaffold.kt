package icu.twtool.chat.material

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import icu.twtool.chat.navigation.window.systemBarWindowInsets

@Composable
fun ICScaffold(
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        topBar = topBar,
        bottomBar = bottomBar,
        content = content,
        contentWindowInsets = systemBarWindowInsets
    )
}