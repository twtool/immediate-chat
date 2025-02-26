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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import icu.twtool.cache.DesktopCache
import icu.twtool.chat.App
import icu.twtool.chat.WebSocketService
import icu.twtool.chat.composition.LocalComposeWindow
import icu.twtool.chat.constants.ApplicationDir
import icu.twtool.chat.constants.COS_CONFIG
import icu.twtool.chat.navigation.window.LocalWindowState
import icu.twtool.chat.navigation.window.calculateWindowSizeClass
import icu.twtool.chat.navigation.window.systemBarHeight
import icu.twtool.chat.theme.ICTheme
import icu.twtool.chat.utils.KeyEventStore
import icu.twtool.chat.utils.LocalKeyEventStore
import icu.twtool.chat.utils.loadLibrary
import icu.twtool.cos.DesktopCosClient
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.app_name
import immediatechat.app.generated.resources.logo
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun main() = runBlocking {
    loadLibrary("mmkv")
    DesktopCache.initialize("$ApplicationDir/mmkv")
    DesktopCosClient.initialize(COS_CONFIG)

    val service = WebSocketService()
    awaitApplication {
        val trayState = rememberTrayState()

        if (isTraySupported) {
            Tray(painterResource(Res.drawable.logo), state = trayState, tooltip = stringResource(Res.string.app_name))
        }
        service.start(trayState)
        systemBarHeight = 24.dp

        val windowState = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            width = 1000.dp,
            height = 700.dp
        )

        CompositionLocalProvider(LocalKeyEventStore provides KeyEventStore(), LocalWindowState provides windowState) {
            val store = LocalKeyEventStore.current
            Window(
                ::exitApplication,
                state = windowState,
                title = stringResource(Res.string.app_name),
                icon = painterResource("drawable/logo.xml"),
                transparent = true, undecorated = true,
                onKeyEvent = on@{
                    for (handler in store.get()) {
                        if (handler(it)) return@on true
                    }
                    false
                },
            ) {

                val windowSize = calculateWindowSizeClass(windowState)

                var showExitDialog by remember { mutableStateOf(false) }

                CompositionLocalProvider(LocalComposeWindow provides window) {
                    ICTheme {
                        if (showExitDialog) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog = false },
                                title = { Text("确认退出？") },
                                confirmButton = {
                                    TextButton({ exitApplication() }) {
                                        Text("确认")
                                    }
                                }
                            )
                        }

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
        }
    }
    service.destroy()
    DesktopCosClient.close()
}