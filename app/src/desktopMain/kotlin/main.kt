import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        var showExitDialog by remember { mutableStateOf(false) }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("确认退出？") },
                confirmButton = {
                    TextButton({exitApplication()}) {
                        Text("确认")
                    }
                }
            )
        }

        ICTheme {
            Surface(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.small) {
                Box {
                    App(windowSize)
                    WindowDraggableArea(Modifier.height(systemBarHeight).fillMaxWidth()) {
                        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.weight(1f, true))
                            Icon(
                                Icons.Filled.Close,
                                "",
                                Modifier.clickable {
                                    showExitDialog = true
                                }.padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}