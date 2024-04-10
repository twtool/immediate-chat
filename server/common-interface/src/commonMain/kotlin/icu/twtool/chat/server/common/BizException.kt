package icu.twtool.chat.server.common

class BizException(
    val status: Status = CommonStatus.Error,
    val msg: String = status.msg
) : RuntimeException(msg)

fun checkParam(value: Boolean, lazyMessage: () -> String = { CommonStatus.ParamErr.msg }) {
    if (!value) throw BizException(CommonStatus.ParamErr, msg = lazyMessage())
}

fun assertTrue(value: Boolean, status: Status = CommonStatus.Error, lazyMessage: () -> String = { status.msg }) {
    if (!value) throw BizException(status, msg = lazyMessage())
}

fun assertFalse(value: Boolean, status: Status = CommonStatus.Error, lazyMessage: () -> String = { status.msg }) {
    if (value) throw BizException(status, msg = lazyMessage())
}

fun <T> assertNotNull(value: T?, status: Status = CommonStatus.Error, lazyMessage: () -> String = { status.msg }): T {
    if (value == null) throw BizException(status, msg = lazyMessage())
    return value
}