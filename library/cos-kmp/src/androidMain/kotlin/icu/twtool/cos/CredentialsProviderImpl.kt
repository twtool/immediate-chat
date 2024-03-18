package icu.twtool.cos

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials

class CredentialsProviderImpl(private val getTmpCredentials: () -> Credentials) : BasicLifecycleCredentialProvider() {

    override fun fetchNewCredentials(): QCloudLifecycleCredentials {
        val credentials = getTmpCredentials()
        return SessionQCloudCredentials(
            credentials.secretId,
            credentials.secretKey,
            credentials.sessionToken,
            credentials.startTime,
            credentials.expireTime,
        )
    }
}