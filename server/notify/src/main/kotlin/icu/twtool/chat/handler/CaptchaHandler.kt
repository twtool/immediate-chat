package icu.twtool.chat.handler

import icu.twtool.chat.sender.MailSender
import icu.twtool.chat.server.notify.topic.NotifyMessage
import org.intellij.lang.annotations.Language

class CaptchaHandler(private val mailSender: MailSender) : Handler {

    override fun isSupport(message: NotifyMessage): Boolean = message is NotifyMessage.Captcha

    private val subject: String = "即时聊天验证码"

    @Language("HTML")
    private val template: String =
        """
<table style="width: 100%;max-width: 800px;margin: auto">
    <tbody><tr>
        <td>
            <p style="">
                <span style="line-height: 4rem;font-size: 2rem;color: #fc9a02">Immediate Chat</span>
            </p>

            <p style="height: 1px;background: #F0F0F0;"></p>

            <p>
                <span style="font-size: 1.5rem">您的 <span style="color: #fc9a02">【{{ACTION}}】</span> 验证码为：</span>
            </p>

            <p style="text-align: center;">
                <span style="display: inline-block;font-family: monospace;color: #fc9a02;background: #F0F0F0;padding: 1rem 2rem;border-radius: 0.5rem;font-size: 2rem">
                <span style="border-bottom:1px dashed #ccc;z-index:1" onclick="return false;" data="{{CAPTCHA}}">{{CAPTCHA}}</span>
                </span>
            </p>

            <p>
                <span style="">该验证码 {{EFFECTIVE_TIME}}内有效。为了保障您的账户安全，请勿向他人泄漏验证码信息。</span>
            </p>

            <p style="height: 1px;background: #F0F0F0;"></p>

            <div style="text-align: center;font-size: 0.8rem;color: #808080">
                <p style="text-align: center">
                    如您对即时聊天的使用有任何疑问, 请随时与我们联系。
                </p>
            </div>
        </td>
    </tr>
</tbody></table>         
"""

    override fun handle(message: NotifyMessage): Boolean {
        if (message !is NotifyMessage.Captcha) return false
        val content = template.replace("{{ACTION}}", message.action)
            .replace("{{CAPTCHA}}", message.captcha)
            .replace("{{EFFECTIVE_TIME}}", message.effectiveTime)

        mailSender.sendMimeMessage(message.address, subject, content, true)
        return true
    }
}