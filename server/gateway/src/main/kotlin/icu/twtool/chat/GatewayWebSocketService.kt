package icu.twtool.chat

import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.chat.constants.SEND_MESSAGE_TO_TOPIC
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.CHAT_WEBSOCKET_PATH
import icu.twtool.chat.server.common.datetime.currentEpochSeconds
import icu.twtool.chat.server.gateway.param.WebSocketParam
import icu.twtool.chat.server.gateway.param.WebSocketParamType
import icu.twtool.chat.server.gateway.topic.PUSH_MESSAGE_TOPIC
import icu.twtool.chat.server.gateway.topic.PushMessage
import icu.twtool.chat.server.gateway.vo.WebSocketVoType
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.client.service.getService
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import icu.twtool.ktor.cloud.route.websockets.websocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.closeExceptionally
import io.ktor.websocket.readText
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonPrimitive
import org.apache.rocketmq.client.apis.consumer.ConsumeResult
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.*

class GatewayWebSocketService(application: KtorCloudApplication, rocketMQPlugin: RocketMQPlugin) : Closeable {

    private val log = LoggerFactory.getLogger(GatewayWebSocketService::class.java)

    private val connections = Collections.synchronizedMap(LinkedHashMap<Long, Connection>())

    private val accountService by lazy { application.getService<AccountService>() }

    private suspend fun send(addressee: Long, type: WebSocketVoType, content: ByteArray): Boolean {
        return send(addressee, type, content.size) {
            put(content)
        }
    }

    private suspend fun send(
        addressee: Long,
        type: WebSocketVoType,
        extendSize: Int,
        builder: ByteBuffer.() -> Unit
    ): Boolean {
        val connection = connections[addressee] ?: return false

        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES + extendSize)
        buffer.putInt(type.ordinal).builder()
        buffer.flip()
        val result = connection.send(Frame.Binary(true, buffer))
        if (!result) connections.remove(addressee, connection)
        return result
    }

    private val rocketMQConsumer = rocketMQPlugin.getPushConsumer(
        mapOf(
            PUSH_MESSAGE_TOPIC to filterTag(),
            SEND_MESSAGE_TO_TOPIC to filterTag()
        ),
        group = application.config[GatewayIDKey]
    ) {
        val body = ByteArray(it.body.remaining())
        it.body.get(body)
        val bodyString = String(body)

        if (log.isDebugEnabled)
            log.debug("TOPIC: {}, ID: {}, content: {}", it.topic, it.messageId, bodyString)
        when (it.topic) {
            PUSH_MESSAGE_TOPIC -> {
                val message = JSON.decodeFromString<PushMessage>(bodyString)
                runBlocking {
                    send(message.uid, WebSocketVoType.Push, body)
                }
            }

            SEND_MESSAGE_TO_TOPIC -> {
                val message = JSON.decodeFromString<MessageVO>(bodyString)
                runBlocking {
                    val addressee = message.addressee
                    send(addressee, WebSocketVoType.Message, body)
                }
            }
        }

        ConsumeResult.SUCCESS
    }

    init {
        application.websocket(CHAT_WEBSOCKET_PATH) {
            var session: Session? = null
            for (frame in incoming) {
                log.info("Received message: {}", frame)
                if (!this.isActive) break
                if (frame is Frame.Close) {
                    session?.uid?.let { connections.remove(it) }
                    break
                }

                val body = (frame as? Frame.Text ?: continue).readText()
                try {
                    val param = JSON.decodeFromString<WebSocketParam>(body)
                    when (param.type) {
                        WebSocketParamType.AUTH -> {
                            val token = param.content.jsonPrimitive.content
                            val loggedUID = accountService.auth(AuthParam(token)).data
                            if (loggedUID == null) {
                                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not logged in"))
                                break
                            }

                            session = Session(loggedUID, token, this)
                            connections.getOrPut(loggedUID) { Connection(loggedUID) }.add(session)
                            send(loggedUID, WebSocketVoType.AuthSuccess, Long.SIZE_BITS) {
                                putLong(currentEpochSeconds())
                            }
                        }

                        WebSocketParamType.HEARTBEAT -> {
                            session?.heartbeat()
                        }
                    }
                } catch (e: Exception) {
                    log.error(e.message, e)
                    closeExceptionally(e)
                }
            }
        }
    }

    override fun close() {
        rocketMQConsumer.close()
    }
}