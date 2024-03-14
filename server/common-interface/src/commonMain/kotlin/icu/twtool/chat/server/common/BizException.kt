package icu.twtool.chat.server.common

class BizException(
    val status: Status = CommonStatus.Error,
    val msg: String = status.msg
) : RuntimeException(msg)