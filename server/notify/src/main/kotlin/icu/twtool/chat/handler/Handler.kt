package icu.twtool.chat.handler

import icu.twtool.chat.server.notify.topic.NotifyMessage

interface Handler {

    fun isSupport(message: NotifyMessage): Boolean

    fun handle(message: NotifyMessage): Boolean
}