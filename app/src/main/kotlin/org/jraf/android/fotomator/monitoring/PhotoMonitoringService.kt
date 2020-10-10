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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jraf.android.fotomator.data.Database
import org.jraf.android.fotomator.data.Media
import org.jraf.android.fotomator.data.MediaUploadState
import org.jraf.android.fotomator.notification.ONGOING_NOTIFICATION_ID
import org.jraf.android.fotomator.notification.createNotificationChannel
import org.jraf.android.fotomator.notification.createPhotoMonitoringServiceNotification
import org.jraf.android.fotomator.upload.SlackClient
import org.jraf.android.util.log.Log
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject

@AndroidEntryPoint
class PhotoMonitoringService : Service() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mutex = Mutex()

    @Inject
    lateinit var slackClient: SlackClient

    @Inject
    lateinit var database: Database

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
        return object : ContentObserver(mainHandler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val mediaContentUri = getLatestContentFromMediaStore(mediaStoreUri)
                Log.d("$mediaStoreUri has changed uri=$uri mediaContentUri=$mediaContentUri")
                if (mediaContentUri != null) handleMedia(mediaContentUri)
            }
        }
    }

    private val internalMediaContentObserver = createContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI)

    private val externalMediaContentObserver = createContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    private fun startMonitoring() {
        Log.d("isStarted=$isStarted")

        if (isStarted) return
        isStarted = true

        handleLatestContent()

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
            false,
            internalMediaContentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            false,
            externalMediaContentObserver
        )
    }

    private fun handleLatestContent() {
        Log.d()
        val internalMediaContentUri = getLatestContentFromMediaStore(MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        Log.d("internalMediaContentUri=$internalMediaContentUri")
        if (internalMediaContentUri != null) handleMedia(internalMediaContentUri)

        val externalMediaContentUri = getLatestContentFromMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        Log.d("externalMediaContentUri=$externalMediaContentUri")
        if (externalMediaContentUri != null) handleMedia(externalMediaContentUri)
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

    private fun handleMedia(mediaContentUri: Uri) = GlobalScope.launch {
        val allReadyKnown = mutex.withLock {
            val mediaContentUriStr = mediaContentUri.toString()
            val foundMedia = database.mediaDao().getByUrl(mediaContentUriStr)
            if (foundMedia != null) {
                Log.d("Media $mediaContentUriStr already known: ignore")
                true
            } else {
                Log.d("Media $mediaContentUriStr not known: upload")
                val media = Media(uri = mediaContentUriStr, uploadState = MediaUploadState.UPLOADING)
                database.mediaDao().insert(media)
                false
            }
        }
        if (!allReadyKnown) uploadContent(mediaContentUri)
    }

    private suspend fun uploadContent(mediaContentUri: Uri) {
        Log.d("mediaContentUri=$mediaContentUri")

        // XXX Add a few seconds delay because for some reason the file may not be ready when called immediately
        delay(2000)

        val parcelFileDescriptor = try {
            contentResolver.openFileDescriptor(mediaContentUri, "r")
        } catch (e: FileNotFoundException) {
            Log.w("openFileDescriptor threw an exception for mediaContentUri=$mediaContentUri", e)
            null
        }
        if (parcelFileDescriptor == null) {
            Log.w("parcelFileDescriptor is null, give up")
            database.mediaDao().insert(Media(uri = mediaContentUri.toString(), uploadState = MediaUploadState.ERROR))
            return
        }
        val ok = parcelFileDescriptor.use {
            slackClient.uploadFile(
                fileInputStream = FileInputStream(it.fileDescriptor),
                channels = "test"
            )
        }

//        val inputStream = contentResolver.openInputStream(Uri.parse(media.uri))
//        if (inputStream == null) {
//            Log.w("inputStream is null, give up")
//            return
//        }
//        val ok = inputStream.use {
//            slackClient.uploadFile(
//                fileInputStream = it,
//                channels = "test",
//            )
//        }

        Log.d("ok=$ok")
        database.mediaDao().insert(Media(uri = mediaContentUri.toString(), uploadState = if (ok) MediaUploadState.UPLOADED else MediaUploadState.PENDING))
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