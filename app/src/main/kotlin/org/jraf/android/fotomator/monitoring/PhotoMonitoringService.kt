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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.notification.ONGOING_NOTIFICATION_ID
import org.jraf.android.fotomator.notification.createNotificationChannel
import org.jraf.android.fotomator.notification.createPhotoMonitoringServiceNotification
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.upload.SlackClient
import org.jraf.android.util.log.Log

class PhotoMonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val slackClient = SlackClient()
    private lateinit var appPrefs: AppPrefs

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        appPrefs = AppPrefs(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        Log.d()

        if (isMonitoring) return
        isMonitoring = true

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
        isMonitoring = false
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

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
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
                    slackAuthToken = appPrefs.slackAuthToken!!
                )
                Log.d("ok=$ok")
            }
        }
    }

    companion object {
        var isMonitoring: Boolean = false
    }
}