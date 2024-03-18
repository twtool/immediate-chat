package icu.twtool.cos

import com.qcloud.cos.auth.BasicSessionCredentials
import com.qcloud.cos.auth.COSCredentials
import com.qcloud.cos.auth.COSCredentialsProvider
import org.slf4j.LoggerFactory

class CredentialsProviderImpl(private val getTmpCredentials: () -> Credentials) : COSCredentialsProvider {

    private val log = LoggerFactory.getLogger(CredentialsProviderImpl::class.java)

    override fun getCredentials(): COSCredentials {
        val credentials = getTmpCredentials()
        return BasicSessionCredentials(
            credentials.secretId,
            credentials.secretKey,
            credentials.sessionToken
        )
    }

    override fun refresh() {
        log.warn("Refreshing credentials")
    }
}