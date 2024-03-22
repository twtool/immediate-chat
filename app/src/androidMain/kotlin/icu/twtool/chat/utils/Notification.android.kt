package icu.twtool.chat.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import icu.twtool.chat.MainActivity
import icu.twtool.chat.R
import icu.twtool.chat.cache.loadImage
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.chat.model.PlainMessageContent
import icu.twtool.chat.server.chat.vo.MessageVO
import kotlinx.serialization.encodeToString

private const val MESSAGE_CHANNEL_ID = "message-channel"

class AndroidNotification(private val context: Context) : Notification {

    private val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    override suspend fun message(info: AccountInfo, message: MessageVO) {
        val permission = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) PackageManager.PERMISSION_GRANTED
        else ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (permission != PackageManager.PERMISSION_GRANTED) return

        // 为应用程序中的活动创建明确的意图。
        val intent = Intent(context, MainActivity::class.java).apply {
            setAction(Intent.ACTION_MAIN)
            putExtra("SENDER", JSON.encodeToString(info))
            putExtra("UID", message.addressee)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(info.nickname ?: "未命名用户")
            .setLargeIcon(loadImage(info.avatarUrl)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.logo))
            .setContentText(message.content.renderText())
            .run {
                when (val content = message.content) {
                    is PlainMessageContent -> setStyle(NotificationCompat.BigTextStyle().bigText(content.value))

                    else -> this
                }
            }
            .setContentIntent(pendingIntent) // 设置当用户点击通知时触发的意图。
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // TODO: 处理通知 ID
        manager.notify(info.uid.toInt(), builder.build())
    }
}

fun initNotification(context: Context) {
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    // Create the NotificationChannel.
    val name = context.getString(R.string.message_channel_name)
    val descriptionText = context.getString(R.string.message_channel_description)
    val importance = NotificationManager.IMPORTANCE_HIGH // 紧急：发出提示音，并以浮动通知的形式显示。
    val messageChannel = NotificationChannel("message-channel", name, importance)
    messageChannel.description = descriptionText
    // Register the channel with the system. You can't change the importance
    // or other notification behaviors after this.
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(messageChannel)
}