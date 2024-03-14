package icu.twtool.chat

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.*

const val CONNECTION_TIMEOUT_MS = 1000L * 60L * 5L

class Session(
    val token: String,
    private val session: DefaultWebSocketServerSession,
) : DefaultWebSocketServerSession by session {

    private var heartbeat: Long = System.currentTimeMillis()

    fun isAlive(expireAt: Long = System.currentTimeMillis() - CONNECTION_TIMEOUT_MS): Boolean =
        heartbeat > expireAt

    fun heartbeat() {
        this.heartbeat = System.currentTimeMillis()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        return token == other.token
    }

    override fun hashCode(): Int {
        return token.hashCode()
    }
}

class Connection(private val loggedUID: Long) {

    private val log = LoggerFactory.getLogger(Connection::class.java)

    // 互斥锁
    private val mutex = Mutex()
    private val sessions = LinkedHashMap<String, Session>()

    suspend fun add(session: Session) {
        mutex.withLock {
            sessions[session.token]?.close()
            sessions[session.token] = session

            if (log.isDebugEnabled)
                log.debug("Account($loggedUID), session number: ${sessions.size}")
        }
    }

    suspend fun send(frame: Frame): Boolean {
        val expireAt = System.currentTimeMillis() - CONNECTION_TIMEOUT_MS

        var flag = 0

        mutex.withLock {
            val iterator = sessions.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.isAlive(expireAt)) {
                    flag++
                    entry.value.send(frame)
                    continue
                }
                log.info("$entry inactivated.")
                entry.value.close()
                iterator.remove()
            }
        }

        return flag > 0
    }
}
