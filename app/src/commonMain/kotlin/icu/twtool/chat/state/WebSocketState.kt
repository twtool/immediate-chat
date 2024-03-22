package icu.twtool.chat.state

import androidx.compose.runtime.mutableStateOf
import icu.twtool.chat.database.database
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.CHAT_WEBSOCKET_PATH
import icu.twtool.chat.server.common.datetime.epochSeconds
import icu.twtool.chat.server.gateway.param.WebSocketParam
import icu.twtool.chat.server.gateway.param.WebSocketParamType
import icu.twtool.chat.server.gateway.vo.WebSocketVoType
import icu.twtool.chat.service.creator
import icu.twtool.chat.utils.JSON
import icu.twtool.ktor.cloud.client.kmp.websocket.websocket
import icu.twtool.logger.getLogger
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

const val CONNECTION_TIMEOUT_MS = 1000L * 60L * 5L - 5000L

class WebSocketUpdate(
    val type: WebSocketVoType? = null
) {

    override fun toString(): String {
        return "WebSocketUpdate(type=$type)"
    }
}

object WebSocketState {

    private val log = getLogger("WebSocketState")

    private var connecting = false

    var error = mutableStateOf<String?>(null)
        private set

    private val _updated = MutableStateFlow(WebSocketUpdate())
    val updated: StateFlow<WebSocketUpdate> = _updated

    private var session: DefaultClientWebSocketSession? = null
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(supervisorJob + Dispatchers.IO)
    private var heartbeatJob: Job? = null
    private var retry: Int = 0

    fun start() {
        scope.launch {
            reconnect()
        }
    }

    fun destroy() {
        supervisorJob.cancel()
    }

    private suspend fun reconnect() {
        try {
            connect()
        } catch (e: Exception) {
            val err = "网络错误，请检查网络连接"
            error.value = err
            log.error(e.message ?: err)
        }
        connecting = false
        log.info("reconnect after 5 seconds.")
        if (++retry > 1) delay(retry.coerceAtMost(5) * 1000L)
        reconnect()
    }

    private fun startHeartbeat(session: ClientWebSocketSession) {
        heartbeatJob?.cancel() // 关闭原心跳检测
        heartbeatJob = scope.launch {
            delay(CONNECTION_TIMEOUT_MS)
            session.send(JSON.encodeToString(WebSocketParam(WebSocketParamType.HEARTBEAT, JsonNull)))
        }
    }

    private suspend fun getSession(retry: Long = 0): DefaultClientWebSocketSession {
        val result = runCatching { creator.websocket(CHAT_WEBSOCKET_PATH) }
        val session = result.getOrNull()
        if (session != null) return session
        val err = "网络错误，请检查网络连接"
        error.value = err
        log.error(result.exceptionOrNull()?.message ?: err)
        delay(retry * 1500 + 500)
        return getSession(retry + 1)
    }

    private suspend fun connect() {
        if (connecting) return
        connecting = true
        heartbeatJob?.cancel() // 关闭原心跳检测

        val session = getSession()
        connecting = false
        error.value = null
        retry = 0
        this.session = session
        startHeartbeat(session)

        auth(session)

        for (frame in session.incoming) {
            if (frame is Frame.Close) {
                session.close()
                log.info("Closed websocket: $frame")
                break
            }

            frame as? Frame.Binary ?: continue
            val buffer = frame.buffer

            val type = WebSocketVoType.entries[buffer.getInt()]

            when (type) {
                WebSocketVoType.Message -> {
                    val content = ByteArray(buffer.remaining())
                    buffer.get(content)
                    val messageStr = String(content)
                    val message = Json.decodeFromString<MessageVO>(messageStr)
                    val addressee = message.addressee
                    val time = message.createTime.epochSeconds()
                    database.messageDetailsQueries.insertOne(
                        addressee,
                        message.sender,
                        messageStr,
                        time,
                        time
                    )
                    database.messageQueries.updateLastMessageId(addressee, message.sender)
                }
            }
            _updated.emit(WebSocketUpdate(type))
        }
    }

    fun auth() {
        session?.let {
            scope.launch {
                auth(it)
            }
        }
    }

    private suspend fun auth(session: ClientWebSocketSession) {
        val token = LoggedInState.token
        if (token == null) {
            delay(1000L)
            auth(session)
            return
        }
        session.send(
            JSON.encodeToString(
                WebSocketParam(WebSocketParamType.AUTH, JsonPrimitive(token))
            )
        )
    }
}