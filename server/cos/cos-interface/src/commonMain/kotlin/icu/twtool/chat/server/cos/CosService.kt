package icu.twtool.chat.server.cos

import icu.twtool.chat.server.cos.vo.TmpCredentialVO
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("CosService", "cos")
interface CosService {

    @RequestMapping(HttpMethod.Get, "tmp-credential")
    suspend fun getTmpCredential(): TmpCredentialVO

    companion object
}

expect fun CosService.Companion.create(creator: IServiceCreator): CosService