package icu.twtool.chat.server.account.status

import icu.twtool.chat.server.common.Status

enum class AccountStatus(override val code: Int, override val msg: String) : Status {
    AccountNotExists(50101, "账户不存在")
}