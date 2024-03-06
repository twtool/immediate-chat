package icu.twtool.chat.server.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Res<T : Any>(
    val code: Int,
    val msg: String,
    val data: T? = null
) {

    @Transient
    val success: Boolean = code == CommonStatus.Success.code

    constructor(status: Status, msg: String? = null, data: T? = null) : this(status.code, msg ?: status.msg, data)

    companion object {

        fun <T : Any> success(data: T? = null, msg: String? = null): Res<T> = Res(CommonStatus.Success, msg, data)
        fun <T : Any> error(status: Status = CommonStatus.Error, msg: String? = null): Res<T> = Res(status, msg)

        fun <T : Any> result(
            flag: Boolean,
            errorStatus: Status = CommonStatus.Error,
            errorMsg: String? = null,
        ): Res<T> {
            return if (flag) success()
            else error(errorStatus, errorMsg)
        }

        fun <T : Any> result(
            data: T?,
            errorStatus: Status = CommonStatus.Error,
            errorMsg: String? = null,
        ): Res<T> {
            return if (data != null) success(data)
            else error(errorStatus, errorMsg)
        }
    }
}

fun <T : Any> Boolean.result(errorStatus: Status = CommonStatus.Error, errorMsg: String? = null): Res<T> =
    Res.result(this, errorStatus, errorMsg)

fun <T : Any> T?.result(errorStatus: Status = CommonStatus.Error, errorMsg: String? = null): Res<T> =
    Res.result(this, errorStatus, errorMsg)