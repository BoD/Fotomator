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
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.upload.scheduler.UploadScheduler
import org.jraf.android.util.log.Log
import org.jraf.android.util.string.StringUtil
import javax.inject.Inject

@AndroidEntryPoint
class PhotoMonitoringService : Service() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mutex = Mutex()
    private var broadcastReceiver: BroadcastReceiver? = null

    @Inject
    lateinit var uploadScheduler: UploadScheduler

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var appPrefs: AppPrefs

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("isStarted=$isStarted")

        if (isStarted) return START_STICKY
        isStarted = true

        startForeground()
        startMonitoring()
        registerBroadcastReceiver()
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
                val mediaUri = getLatestContentFromMediaStore(mediaStoreUri)
                Log.d("$mediaStoreUri has changed uri=$uri mediaUri=$mediaUri")
                if (mediaUri != null) handleMedia(mediaUri)
            }
        }
    }

    private val internalMediaContentObserver = createContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI)

    private val externalMediaContentObserver = createContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    private fun startMonitoring() {
        Log.d("isStarted=$isStarted")

        handleLatestContent()

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

    private fun handleMedia(mediaUri: Uri) = GlobalScope.launch {
        val allReadyKnown = mutex.withLock {
            val mediaUriStr = mediaUri.toString()
            val foundMedia = database.mediaDao().getByUrl(mediaUriStr)
            if (foundMedia != null) {
                Log.d("Media $mediaUriStr already known: ignore")
                true
            } else {
                Log.d("Media $mediaUriStr not known: schedule")
                val media = Media(uri = mediaUriStr, uploadState = MediaUploadState.SCHEDULED)
                database.mediaDao().insert(media)
                false
            }
        }

        // XXX Add a small delay to ensure the media is ready to be read
        // Not 100% sure why this is needed
        delay(2000)

        if (!allReadyKnown) uploadScheduler.addToSchedule(mediaUri)
    }

    private fun registerBroadcastReceiver() {
        Log.d()
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("intent=${StringUtil.toString(intent)}")
                when (intent.action) {
                    ACTION_STOP_SERVICE -> {
                        appPrefs.isServiceEnabled.value = false
                        stopSelf()
                    }

                    ACTION_OPT_OUT -> {
                        val mediaUri: Uri = intent.getParcelableExtra(EXTRA_MEDIA_URI)!!
                        val media = Media(uri = mediaUri.toString(), uploadState = MediaUploadState.OPT_OUT)
                        GlobalScope.launch {
                            database.mediaDao().insert(media)
                        }
                        uploadScheduler.removeFromSchedule(mediaUri)
                    }

                    ACTION_UPLOAD_IMMEDIATELY -> {
                        val mediaUri: Uri = intent.getParcelableExtra(EXTRA_MEDIA_URI)!!
                        uploadScheduler.uploadImmediately(mediaUri)
                    }
                }
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(ACTION_STOP_SERVICE)
            addAction(ACTION_OPT_OUT)
            addAction(ACTION_UPLOAD_IMMEDIATELY)
        })
    }

    private fun unregisterBroadcastReceiver() {
        broadcastReceiver?.let { unregisterReceiver(it) }
        broadcastReceiver = null
    }

    override fun onDestroy() {
        Log.d("isStarted=$isStarted")
        isStarted = false
        stopMonitoring()
        unregisterBroadcastReceiver()
        GlobalScope.launch {
            uploadScheduler.removeAllFromSchedule()
        }
        super.onDestroy()
    }

    companion object {
        var isStarted: Boolean = false

        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_OPT_OUT = "ACTION_OPT_OUT"
        const val ACTION_UPLOAD_IMMEDIATELY = "ACTION_UPLOAD_IMMEDIATELY"
        const val EXTRA_MEDIA_URI = "EXTRA_MEDIA_URI"
    }
}