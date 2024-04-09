package icu.twtool.chat.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import icu.twtool.chat.database.database
import icu.twtool.chat.server.chat.ChatService
import icu.twtool.chat.server.chat.model.AccountMessageAddressee
import icu.twtool.chat.server.chat.model.MessageContent
import icu.twtool.chat.server.chat.param.SendMessageParam
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.datetime.epochSeconds
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.server.gateway.vo.WebSocketVoType
import icu.twtool.chat.service.get
import icu.twtool.chat.utils.JSON
import icu.twtool.chat.utils.formatLocal
import icu.twtool.chat.utils.tryLockRun
import icu.twtool.logger.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlin.math.max

data class ChatMessageItem(
    val id: Long,
    val me: Boolean,
    val message: MessageVO,
    val createTimeFormat: String
) {

    companion object {

        fun from(id: Long, loggedUID: Long, messageVO: MessageVO): ChatMessageItem {
            return ChatMessageItem(
                id,
                loggedUID == messageVO.sender,
                messageVO,
                createTimeFormat = messageVO.createTime.formatLocal()
            )
        }
    }
}

@Stable
class ChatViewState(scope: CoroutineScope, private val friendUID: Long) {

    private val log = getLogger("ChatViewState")

    private val mutex = Mutex()

    var sending by mutableStateOf(false)
        private set

    val messages = mutableStateListOf<ChatMessageItem>()
    private var lastMessageID = 0L

    private suspend fun lockSelectNow(limit: Long? = null) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                selectNow(limit)
            }
        }
    }

    private fun selectNow(limit: Long? = null) {
        val loggedUID = LoggedInState.info?.uid ?: return
        val mapper = { id: Long, lu: Long, _: Long, message: String, _: Long, _: Long ->
            lastMessageID = max(lastMessageID, id)
            ChatMessageItem.from(id, lu, JSON.decodeFromString(message))
        }
        val items = database.messageDetailsQueries
            .run {
                if (limit != null) selectNewLimit(loggedUID, friendUID, lastMessageID, limit, mapper)
                else selectNew(loggedUID, friendUID, lastMessageID, mapper)
            }
            .executeAsList()

        if (items.isNotEmpty()) {
            messages.addAll(0, items)
        }
    }

    init {
        scope.launch {
            lockSelectNow(20)
            WebSocketState.updated.collectLatest {
                log.info("updated state: $it")
                if (it.type == WebSocketVoType.Message) {
                    lockSelectNow()
                }
            }
        }
    }

    private val chatService: ChatService by lazy { ChatService.get() }

    suspend fun send(content: MessageContent, friendUID: Long) {
        withContext(Dispatchers.IO) {
            mutex.tryLockRun {
                if (sending) return@tryLockRun
                sending = true
                val loggedUID = LoggedInState.info?.uid ?: return@tryLockRun

                val now = LocalDateTime.nowUTC()
                val nowEpochSeconds = now.epochSeconds()

                val message = MessageVO(
                    sender = loggedUID, addressee = loggedUID,
                    originAddressee = AccountMessageAddressee(friendUID),
                    content = content,
                    createTime = now
                )

                database.messageDetailsQueries.insertOne(
                    loggedUID, friendUID,
                    JSON.encodeToString(message),
                    nowEpochSeconds, nowEpochSeconds
                )

                selectNow()

                database.messageQueries.updateLastMessageId(loggedUID, friendUID)

                val param = SendMessageParam(addressee = message.originAddressee, content = content)

                val res = chatService.sendMessage(param)
                if (res.success) {
                    // TODO
                }

                log.info("res = $res")
            }
            sending = false
        }
    }
}