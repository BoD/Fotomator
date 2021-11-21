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
package org.jraf.android.fotomator.upload.scheduler

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jraf.android.fotomator.data.Database
import org.jraf.android.fotomator.data.MediaUploadState
import org.jraf.android.fotomator.notification.createPhotoScheduledNotification
import org.jraf.android.fotomator.notification.createPhotoUploadingNotification
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.upload.client.slack.SlackClient
import org.jraf.android.util.log.Log
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val slackClient: SlackClient,
    private val database: Database,
    private val appPrefs: AppPrefs,
) {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val scheduledTasks = mutableMapOf<Uri, ScheduledFuture<*>>()

    fun addToSchedule(mediaUri: Uri) {
        val delayMs = getScheduledTaskDelayMs()
        Log.d("Scheduling upload mediaUri=$mediaUri in delayMs=$delayMs")

        val notificationSuccess = showScheduledNotification(mediaUri, delayMs)
        if (!notificationSuccess) {
            Log.d("Could not show notification: don't schedule")
            return
        }

        val scheduledFuture = scheduler.schedule({
            runBlocking {
                uploadContent(mediaUri)
            }
        }, delayMs, TimeUnit.MILLISECONDS)
        scheduledTasks[mediaUri] = scheduledFuture
    }

    fun removeFromSchedule(mediaUri: Uri) {
        Log.d("mediaUri=$mediaUri")
        val previousValue = scheduledTasks.remove(mediaUri)
        previousValue?.cancel(true)
        hideScheduledNotification(mediaUri)
    }

    suspend fun removeAllFromSchedule() {
        Log.d()
        val iterator = scheduledTasks.iterator()
        while (iterator.hasNext()) {
            val (mediaUri, future) = iterator.next()
            future.cancel(true)
            hideScheduledNotification(mediaUri)
            iterator.remove()
        }
        database.mediaDao().updateAllScheduledAsOptOut()
    }

    fun uploadImmediately(mediaUri: Uri) {
        Log.d("mediaUri=$mediaUri")
        val previousValue = scheduledTasks.remove(mediaUri)
        previousValue?.cancel(true)

        GlobalScope.launch {
            uploadContent(mediaUri)
        }
    }

    private suspend fun uploadContent(mediaUri: Uri) {
        Log.d("mediaUri=$mediaUri")

        val media = database.mediaDao().getByUrl(mediaUri.toString())!!

        // Update notification
        val notificationSuccess = showUploadingNotification(mediaUri)
        if (!notificationSuccess) {
            // Could not show the notification: probably can't read from the media. Give up.
            database.mediaDao().insert(media.copy(uploadState = MediaUploadState.ERROR))
            return
        }

        // Update the db
        database.mediaDao().insert(media.copy(uploadState = MediaUploadState.UPLOADING))

        val parcelFileDescriptor = try {
            @Suppress("BlockingMethodInNonBlockingContext")
            context.contentResolver.openFileDescriptor(mediaUri, "r")
        } catch (e: FileNotFoundException) {
            Log.w("openFileDescriptor threw an exception for mediaUri=$mediaUri", e)
            null
        }
        if (parcelFileDescriptor == null) {
            Log.w("parcelFileDescriptor is null, give up")
            database.mediaDao().insert(media.copy(uploadState = MediaUploadState.ERROR))
            return
        }
        val ok = parcelFileDescriptor.use {
            slackClient.uploadFile(
                fileInputStream = FileInputStream(it.fileDescriptor),
                channelId = appPrefs.slackChannelId!!
            )
        }

        Log.d("ok=$ok")

        if (!ok) {
            val uploadFailedCount = media.uploadFailedCount + 1
            Log.d("uploadFailedCount=$uploadFailedCount")
            if (uploadFailedCount >= MAX_UPLOAD_FAILED_BEFORE_GIVING_UP) {
                Log.d("Upload failed too many times: give up")

                // Hide notification
                hideScheduledNotification(mediaUri)

                database.mediaDao().insert(
                    media.copy(
                        uploadState = MediaUploadState.ERROR,
                        uploadFailedCount = uploadFailedCount
                    )
                )
                scheduledTasks.remove(mediaUri)
            } else {
                Log.d("Upload failed: re-schedule")
                database.mediaDao().insert(
                    media.copy(
                        uploadState = MediaUploadState.SCHEDULED,
                        uploadFailedCount = uploadFailedCount
                    )
                )
                addToSchedule(mediaUri)
            }
        } else {
            // Hide notification
            hideScheduledNotification(mediaUri)

            database.mediaDao().insert(
                media.copy(uploadState = MediaUploadState.UPLOADED)
            )
            scheduledTasks.remove(mediaUri)
        }
    }

    private fun showScheduledNotification(mediaUri: Uri, delayMs: Long): Boolean {
        Log.d("mediaUri=$mediaUri")
        val notification = createPhotoScheduledNotification(context, mediaUri, delayMs) ?: return false
        NotificationManagerCompat.from(context).notify(mediaUri.toNotificationId(), notification)
        return true
    }

    private fun showUploadingNotification(mediaUri: Uri): Boolean {
        Log.d("mediaUri=$mediaUri")
        val notification = createPhotoUploadingNotification(context, mediaUri) ?: return false
        NotificationManagerCompat.from(context).notify(mediaUri.toNotificationId(), notification)
        return true
    }

    private fun hideScheduledNotification(mediaUri: Uri) {
        Log.d("mediaUri=$mediaUri")
        NotificationManagerCompat.from(context).cancel(mediaUri.toNotificationId())
    }

    private fun getScheduledTaskDelayMs() = DEFAULT_DELAY_MS

    companion object {
        private val DEFAULT_DELAY_MS = TimeUnit.MINUTES.toMillis(1)
        private const val MAX_UPLOAD_FAILED_BEFORE_GIVING_UP = 5
    }
}

private fun Uri.toNotificationId(): Int {
    return toString().hashCode()
}
