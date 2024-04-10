package icu.twtool.chat.sender

import icu.twtool.chat.NOTIFY_MAIL_PASSWORD
import icu.twtool.chat.NOTIFY_MAIL_PORT
import icu.twtool.chat.NOTIFY_MAIL_SMTP_HOST
import icu.twtool.chat.NOTIFY_MAIL_USER
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.Plugin
import io.ktor.util.AttributeKey
import jakarta.mail.Authenticator
import jakarta.mail.BodyPart
import jakarta.mail.MessagingException
import jakarta.mail.Multipart
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.util.*

val MailSenderKey =
    AttributeKey<MailSender>("icu.twtool.chat.server.mail.sender")

class MailSender : Plugin {

    // 发送邮件的环境属性
    private val props = Properties().apply {
        // 表示SMTP发送邮件，需要进行身份验证
        put("mail.smtp.auth", "true")
        // 如果使用ssl，则去掉使用25端口的配置，进行如下配置,
        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        setProperty("mail.smtp.socketFactory.fallback", "false");
        put("mail.smtp.ssl.enable", "true");
        //put("mail.smtp.starttls.enable","true");
    }

    private val session: Session by lazy {
        // 构建授权信息，用于进行SMTP进行身份验证
        val authenticator: Authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                // 用户名、密码
                val userName = props.getProperty("mail.user")
                val password = props.getProperty("mail.password")
                return PasswordAuthentication(userName, password)
            }
        }

        Session.getInstance(props, authenticator)
    }

    override fun KtorCloudApplication.install() {
        application.attributes.put(MailSenderKey, this@MailSender)

        props["mail.smtp.host"] = config[NOTIFY_MAIL_SMTP_HOST]

        val smtpPort = config[NOTIFY_MAIL_PORT].toString()
        props["mail.smtp.socketFactory.port"] = smtpPort
        props["mail.smtp.port"] = smtpPort

        // 发件人的账号，填写控制台配置的发信地址,比如xxx@xxx.com
        props["mail.user"] = config[NOTIFY_MAIL_USER]
        // 访问SMTP服务时需要提供的密码
        props["mail.password"] = config[NOTIFY_MAIL_PASSWORD]
    }

    fun sendMimeMessage(address: String, subject: String, content: String, html: Boolean = false) {
        val uuid = UUID.randomUUID()
        val messageIDValue = "<$uuid>"

        //创建邮件消息
        val message: MimeMessage = object : MimeMessage(session) {
            @Throws(MessagingException::class)
            override fun updateMessageID() {
                //设置自定义Message-ID值
                setHeader("Message-ID", messageIDValue)
            }
        }

        // 设置发件人邮件地址和名称。填写控制台配置的发信地址,和mail.user保持一致。发信别名可以自定义，如test。
        val from = InternetAddress(props.getProperty("mail.user"), "即时聊天")
        message.setFrom(from)

        //设置收件人邮件地址，比如yyy@yyy.com
        val to = InternetAddress(address)
        message.setRecipient(MimeMessage.RecipientType.TO, to)

        // 设置邮件标题
        message.subject = subject
        //发送附件，总的邮件大小不超过10M，创建消息部分
        val messageBodyPart: BodyPart = MimeBodyPart()
        //消息 text/plain（纯文本）text/html（HTML 文档）
        messageBodyPart.setText(content)
        messageBodyPart.setHeader("Content-Type", "text/${if (html) "html" else "plain"};charset=utf-8")

        //创建多重消息
        val multipart: Multipart = MimeMultipart()
        //设置文本消息部分
        multipart.addBodyPart(messageBodyPart)

        //发送可含有附件的完整消息
        message.setContent(multipart)
        //发送邮件
        Transport.send(message)
    }

    companion object {
        context(KtorCloudApplication)
        fun install() {
            install(MailSender())
        }

        context(KtorCloudApplication)
        fun getInstance(): MailSender = application.attributes[MailSenderKey]
    }
}