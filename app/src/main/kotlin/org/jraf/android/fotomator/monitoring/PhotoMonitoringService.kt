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
package org.jraf.android.fotomator.monitoring

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.BaseColumns
import android.provider.MediaStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.notification.ONGOING_NOTIFICATION_ID
import org.jraf.android.fotomator.notification.createNotificationChannel
import org.jraf.android.fotomator.notification.createPhotoMonitoringServiceNotification
import org.jraf.android.fotomator.upload.SlackClient
import org.jraf.android.util.log.Log
import javax.inject.Inject

@AndroidEntryPoint
class PhotoMonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var slackClient: SlackClient

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("isStarted=$isStarted")
        startForeground()
        startMonitoring()
        return START_STICKY
    }

    private fun startForeground() {
        createNotificationChannel(this)
        val notification = createPhotoMonitoringServiceNotification(this)
        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createContentObserver(mediaStoreUri: Uri): ContentObserver {
        return object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val mediaContentUri = getLatestContentFromMediaStore(mediaStoreUri)
                Log.d("$mediaStoreUri has changed uri=$uri mediaContentUri=$mediaContentUri")
                mediaContentUri?.let { uploadContent(it) }
            }
        }
    }

    private val internalMediaContentObserver = createContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI)

    private val externalMediaContentObserver = createContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    private fun startMonitoring() {
        Log.d("isStarted=$isStarted")

        if (isStarted) return
        isStarted = true

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
            true,
            internalMediaContentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            externalMediaContentObserver
        )
    }

    private fun stopMonitoring() {
        Log.d()
        contentResolver.unregisterContentObserver(internalMediaContentObserver)
        contentResolver.unregisterContentObserver(externalMediaContentObserver)
    }

    private fun getLatestContentFromMediaStore(mediaStoreUri: Uri): Uri? {
        Log.d("mediaStoreUri=$mediaStoreUri")
        val cursor = contentResolver.query(mediaStoreUri, arrayOf(BaseColumns._ID), null, null, "${MediaStore.MediaColumns.DATE_ADDED} DESC")!!
        var id: Long = -1L
        if (cursor.moveToNext()) {
            id = cursor.getLong(0)
        }
        cursor.close()
        return if (id == -1L) null else ContentUris.withAppendedId(mediaStoreUri, id)
    }

    private fun uploadContent(mediaContentUri: Uri) {
        Log.d("mediaContentUri=$mediaContentUri")
//        val parcelFileDescriptor = contentResolver.openFileDescriptor(mediaContentUri, "r")
//        if (parcelFileDescriptor == null) {
//            Log.w("parcelFileDescriptor is null, give up")
//            return
//        }
//        GlobalScope.launch {
//            parcelFileDescriptor.use {
//                val ok = slackClient.uploadFile(
//                    fileInputStream = FileInputStream(it.fileDescriptor),
//                    channels = "test",
//                    slackAuthToken = appPrefs.slackAuthToken!!
//                )
//                Log.d("ok=$ok")
//            }
//        }

        val inputStream = contentResolver.openInputStream(mediaContentUri)
        if (inputStream == null) {
            Log.w("inputStream is null, give up")
            return
        }
        GlobalScope.launch {
            inputStream.use {
                val ok = slackClient.uploadFile(
                    fileInputStream = it,
                    channels = "test",
                )
                Log.d("ok=$ok")
            }
        }
    }

    override fun onDestroy() {
        Log.d("isStarted=$isStarted")
        isStarted = false
        stopMonitoring()
        super.onDestroy()
    }

    companion object {
        var isStarted: Boolean = false
    }
}