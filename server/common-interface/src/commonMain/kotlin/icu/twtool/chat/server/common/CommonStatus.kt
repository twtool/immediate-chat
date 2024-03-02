package icu.twtool.chat.server.common

enum class CommonStatus(
    override val code: Int,
    override val msg: String
) : Status {
    Success(2000, "请求成功"),
    Error(5000, "系统错误"),
    Timeout(5001, "请求超时"),
}