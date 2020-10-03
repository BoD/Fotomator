package org.jraf.android.fotomator.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.main.MainActivity

const val NOTIFICATION_CHANNEL_MAIN = "NOTIFICATION_CHANNEL_MAIN"

const val ONGOING_NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_MAIN,
        context.getString(R.string.notification_channel_main_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        this.description = context.getString(R.string.notification_channel_main_description)
        setSound(null, null)
    }
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun createPhotoMonitoringServiceNotification(context: Context): Notification {
    val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MAIN)
        .setContentTitle(context.getString(R.string.photoMonitoringService_notification_title))
        .setContentText(context.getString(R.string.photoMonitoringService_notification_text))
        .setSmallIcon(R.drawable.ic_notification_24)
        .setContentIntent(pendingIntent)
        .setShowWhen(false)
        .setTicker(context.getString(R.string.photoMonitoringService_notification_title))
        .build()
}
