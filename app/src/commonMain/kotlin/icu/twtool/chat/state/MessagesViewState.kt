package icu.twtool.chat.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import icu.twtool.chat.database.database
import icu.twtool.chat.server.chat.model.MessageContent
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.datetime.now
import icu.twtool.chat.utils.JSON
import icu.twtool.chat.utils.tryLockRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

val MessageTimeTimeFormat: DateTimeFormat<LocalDateTime> by lazy {
    LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }
}
val MessageTimeDateFormat: DateTimeFormat<LocalDateTime> by lazy {
    LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
    }
}

@Stable
data class MessageItem(
    val uid: Long,
    val avatarUrl: String?,
    val nickname: String?,
    val message: String?,
    val updateAt: String
)

@Stable
class MessagesViewState {

    private val mutex = Mutex()

    val messageItemList = mutableStateListOf<MessageItem>()

    suspend fun loadList(refresh: Boolean = false) {
        val loggedUID = LoggedInState.info?.uid ?: return
        withContext(Dispatchers.IO) {
            mutex.tryLockRun {
                val systemTZ = TimeZone.currentSystemDefault()
                val systemNow = LocalDateTime.now(systemTZ)
                database.messageQueries.selectMessageItemByLoggedUID(loggedUID) { uid, message, updateAt, messageUpdateAt, nickname, avatarUrl ->
                    val decodedMessage = message?.let { JSON.decodeFromString<MessageVO>(it).content }?.toString()
                    val updateAtInstant = Instant.fromEpochSeconds(messageUpdateAt ?: updateAt)
                    val updateAtTime = updateAtInstant.toLocalDateTime(systemTZ)
                    val time = if (updateAtTime.date == systemNow.date) updateAtTime.format(MessageTimeTimeFormat)
                    else updateAtTime.format(MessageTimeDateFormat)
                    MessageItem(
                        uid = uid,
                        avatarUrl = avatarUrl,
                        nickname = nickname,
                        message = decodedMessage,
                        updateAt = time
                    )
                }.executeAsList().let {
                    messageItemList.clear()
                    messageItemList.addAll(it)
                }
            }
        }
    }
}