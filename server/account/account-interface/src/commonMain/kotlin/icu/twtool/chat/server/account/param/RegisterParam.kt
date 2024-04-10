package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class RegisterParam(
    val email: String,
    val captcha: String,
    val pwd: String,
) {

    companion object {

        fun verifyPwd(pwd: String): String? {
            if (pwd.contains(Regex("\\s"))) {
                return "密码不能包含空格"
            }

            if (pwd.length < 8 || pwd.length > 32) {
                return "密码长度在 8-32 位之间"
            }

            return null
        }
    }
}
