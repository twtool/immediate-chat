package icu.twtool.chat.constants

import icu.twtool.chat.server.cos.CosService
import icu.twtool.chat.service.creator
import icu.twtool.chat.service.get
import icu.twtool.cos.CosConfig
import icu.twtool.cos.Credentials
import kotlinx.coroutines.runBlocking

val COS_CONFIG = CosConfig(
    "ap-shanghai",
    "immediate-chat-1256498121",
    getTmpCredentials = {
        runBlocking {
            CosService.get().getTmpCredential().let {
                Credentials(
                    it.secretId,
                    it.secretKey,
                    it.sessionToken,
                    it.startTime,
                    it.expiredTime
                )
            }
        }
    }
)