package icu.twtool.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.app.LoginRoute
import icu.twtool.chat.app.MessagesRoute
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.navigation.window.calculateWindowSizeClass
import icu.twtool.chat.navigation.window.currentWindowSizeClass
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ICTheme
import icu.twtool.chat.utils.JSON
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private val navController = NavController(if (LoggedInState.token == null) LoginRoute else MessagesRoute)

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent")
        runCatching {
            intent?.getLongExtra("UID", -1)?.takeIf { it != -1L && it == LoggedInState.info?.uid }?.let {
                intent.getStringExtra("SENDER")?.let { JSON.decodeFromString<AccountInfo>(it) }?.let { sender ->
                    if (ChatRoute.info?.uid == sender.uid) return@runCatching
                    scope.launch {
                        ChatRoute.open(sender) {
                            withContext(Dispatchers.Main) {
                                if (currentWindowSizeClass(this@MainActivity).widthSizeClass == ICWindowWidthSizeClass.Expanded) {
                                    navController.navigateTo(ChatRoute, listOf(MessagesRoute))
                                } else {
                                    navController.navigateTo(ChatRoute)
                                }
                            }
                        }
                    }
                }
            }
        }.let {
            if (it.isFailure) {
                Log.e(TAG, "onNewIntent", it.exceptionOrNull())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        startService(Intent(this, WebSocketService::class.java))

        setContent {
            val windowSize = calculateWindowSizeClass(this)
            ICTheme {
                enableEdgeToEdge()

                RequestNotificationPermission()
                App(windowSize, navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)

        if (!notificationPermission.status.isGranted) {
            if (!notificationPermission.status.shouldShowRationale) {
                LaunchedEffect(notificationPermission.status) {
                    notificationPermission.launchPermissionRequest()
                }
            }
        }
    }
}