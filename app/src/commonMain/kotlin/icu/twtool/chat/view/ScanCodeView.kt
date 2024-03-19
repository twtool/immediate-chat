package icu.twtool.chat.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.service.get
import icu.twtool.logger.getLogger
import kotlinx.coroutines.launch

private val log = getLogger("ScanCodeView")

@Composable
expect fun CameraView(onScanUID: (String) -> Unit)

@Composable
fun ScanCodeView(navigateToAccountInfoRoute: () -> Unit) {
    val scope = rememberCoroutineScope()
    CameraView(onScanUID = {
        scope.launch {
            val res = AccountService.get().getInfoByUID(it)

            if (res.success) {
                res.data?.let { info ->
                    AccountInfoRoute.open(info) {
                        navigateToAccountInfoRoute()
                    }
                }
            }
            log.info("res = $res")
        }
    })
}