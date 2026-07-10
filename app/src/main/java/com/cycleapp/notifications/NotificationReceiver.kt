package com.cycleapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cycleapp.MainActivity
import com.cycleapp.R

const val NOTIF_CHANNEL_ID = "cycle_reminders"
const val EXTRA_TITLE = "extra_title"
const val EXTRA_MESSAGE = "extra_message"
const val EXTRA_NOTIF_ID = "extra_notif_id"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Цикл и Энергия"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0)

        ensureChannel(context)

        val contentIntent = android.app.PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
            // Разрешение на уведомления не выдано — просто ничего не показываем
        }
    }

    companion object {
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(NOTIF_CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        NOTIF_CHANNEL_ID,
                        "Напоминания о цикле",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Напоминания о менструации и овуляции"
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
}
