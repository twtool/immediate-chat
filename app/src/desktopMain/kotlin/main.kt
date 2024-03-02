import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import icu.twtool.cache.DesktopCache
import icu.twtool.chat.App
import icu.twtool.chat.constants.ApplicationDir
import icu.twtool.chat.navigation.window.calculateWindowSizeClass
import icu.twtool.chat.navigation.window.systemBarHeight
import icu.twtool.chat.theme.ICTheme
import icu.twtool.chat.utils.loadLibrary

fun main() = application {
    systemBarHeight = 24.dp
    loadLibrary("mmkv")
    DesktopCache.initialize("$ApplicationDir\\mmkv")

    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        width = 840.dp,
    )

    Window(
        ::exitApplication, state = windowState,
        title = "即时聊天",
        icon = painterResource("drawable/logo.xml"),
        transparent = true, undecorated = true
    ) {
        val windowSize = calculateWindowSizeClass(windowState)

        ICTheme {
            Surface(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.small) {
                Box {
                    App(windowSize)
                    WindowDraggableArea(Modifier.height(systemBarHeight).fillMaxWidth()) {
                    }
                }
            }
        }

    }
}