/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2020-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.fotomator.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.main.MainActivity
import org.jraf.android.fotomator.monitoring.PhotoMonitoringService
import org.jraf.android.fotomator.util.loadBitmapFromUri
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random


const val NOTIFICATION_CHANNEL_MAIN = "NOTIFICATION_CHANNEL_MAIN"

const val ONGOING_NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_MAIN,
        context.getString(R.string.notification_channel_main_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(R.string.notification_channel_main_description)
        setSound(null, null)
    }
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.createNotificationChannel(channel)
}

fun createPhotoMonitoringServiceNotification(context: Context, automaticallyStopServiceDateTime: Long?): Notification {
    val mainActivityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MAIN)
        .setContentTitle(context.getString(R.string.notification_service_title))
        .setContentText(context.getString(R.string.notification_service_text_withoutEndDate))
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(
                if (automaticallyStopServiceDateTime == null) {
                    context.getString(R.string.notification_service_text_withoutEndDate)
                } else {
                    val formattedDateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(automaticallyStopServiceDateTime))
                    context.getString(R.string.notification_service_text_withEndDate, formattedDateTime)
                }
            )
        )
        .setSmallIcon(R.drawable.ic_notification_24)
        .setContentIntent(mainActivityPendingIntent)
        .setShowWhen(false)
        .setTicker(context.getString(R.string.notification_service_title))
        .setColor(context.getColor(R.color.md_theme_light_primary))
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .addAction(
            R.drawable.ic_stop_service_24,
            context.getString(R.string.notification_service_action_stop),
            PendingIntent.getBroadcast(
                context,
                uniqueRequestCode(),
                Intent(PhotoMonitoringService.ACTION_STOP_SERVICE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
}

fun createPhotoScheduledNotification(
    context: Context,
    mediaUri: Uri,
    scheduledTaskDelayMs: Long,
): Notification? {
    val mainActivityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = context.getString(R.string.notification_scheduled_title)

    val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(scheduledTaskDelayMs)
    val delayMinutesStr = context.resources.getQuantityString(
        R.plurals.notification_scheduled_text_delay,
        delayMinutes.toInt(), delayMinutes
    )
    val text = context.getString(R.string.notification_scheduled_text, delayMinutesStr)

    val photoBitmap = loadBitmapFromUri(context, mediaUri) ?: return null

    val bigPictureStyle = createBigPictureStyle(photoBitmap, title, text)

    // TODO group notifications
    // See https://developer.android.com/training/notify-user/group

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MAIN)
        .setStyle(bigPictureStyle)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_notification_24)
        .setLargeIcon(photoBitmap)
        .setContentIntent(mainActivityPendingIntent)
        .setShowWhen(false)
        .setTicker(title)
        .setColor(context.getColor(R.color.md_theme_light_primary))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(
            R.drawable.ic_opt_out_24,
            context.getString(R.string.notification_scheduled_action_optOut),
            PendingIntent.getBroadcast(
                context,
                uniqueRequestCode(),
                Intent(PhotoMonitoringService.ACTION_OPT_OUT)
                    .putExtra(PhotoMonitoringService.EXTRA_MEDIA_URI, mediaUri),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            R.drawable.ic_upload_immediately_24,
            context.getString(R.string.notification_scheduled_action_uploadImmediately),
            PendingIntent.getBroadcast(
                context,
                uniqueRequestCode(),
                Intent(PhotoMonitoringService.ACTION_UPLOAD_IMMEDIATELY)
                    .putExtra(PhotoMonitoringService.EXTRA_MEDIA_URI, mediaUri),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setOngoing(true)
        .build()
}

fun createPhotoUploadingNotification(
    context: Context,
    mediaUri: Uri,
): Notification? {
    val mainActivityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = context.getString(R.string.notification_uploading_title)
    val text = context.getString(R.string.notification_uploading_text)

    val photoBitmap = loadBitmapFromUri(context, mediaUri) ?: return null

    val bigPictureStyle = createBigPictureStyle(photoBitmap, title, text)

    // TODO group notifications
    // See https://developer.android.com/training/notify-user/group

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MAIN)
        .setStyle(bigPictureStyle)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_notification_24)
        .setLargeIcon(photoBitmap)
        .setContentIntent(mainActivityPendingIntent)
        .setShowWhen(false)
        .setTicker(title)
        .setColor(context.getColor(R.color.md_theme_light_primary))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(true)
        .build()
}


private fun createBigPictureStyle(
    photoBitmap: Bitmap,
    title: String,
    text: String,
): NotificationCompat.BigPictureStyle {
    return NotificationCompat.BigPictureStyle()
        .bigPicture(photoBitmap)
        .bigLargeIcon(null)
        .setBigContentTitle(title)
        .setSummaryText(text)
}

fun uniqueRequestCode() = Random.nextInt()