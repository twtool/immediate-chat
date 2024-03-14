package icu.twtool.chat.server.account.interceptor

import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.CommonStatus
import icu.twtool.ktor.cloud.KtorCloudApplication
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.header
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class LoggedInAccountInfoElement(val value: AccountInfo) :
    AbstractCoroutineContextElement(LoggedInAccountInfoElement) {
    companion object Key : CoroutineContext.Key<LoggedInAccountInfoElement>
}

suspend fun loggedAccountInfo(): AccountInfo =
    coroutineContext[LoggedInAccountInfoElement]?.value ?: throw BizException(CommonStatus.NotLogged)

suspend fun loggedUID(): Long = loggedAccountInfo().uid

fun KtorCloudApplication.installTokenInterceptor() {
    application.intercept(ApplicationCallPipeline.Plugins) {
        val authorization = call.request.header(HttpHeaders.Authorization) ?: return@intercept proceed()
        authorization.let {
            val info = Jwt.parse(it.replaceFirst("Bearer ", "")).payload.account
            withContext(coroutineContext + LoggedInAccountInfoElement(info)) {
                proceed()
            }
        }
    }
}