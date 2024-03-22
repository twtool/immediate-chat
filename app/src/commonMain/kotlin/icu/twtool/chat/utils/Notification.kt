package icu.twtool.chat.utils

import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.chat.vo.MessageVO

interface Notification {

    suspend fun message(info: AccountInfo, message: MessageVO)
}