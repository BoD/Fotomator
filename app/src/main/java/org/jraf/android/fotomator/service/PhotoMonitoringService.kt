package org.jraf.android.fotomator.service

import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import org.jraf.android.fotomator.notification.ONGOING_NOTIFICATION_ID
import org.jraf.android.fotomator.notification.createNotificationChannel
import org.jraf.android.fotomator.notification.createPhotoMonitoringServiceNotification
import org.jraf.android.util.log.Log

class PhotoMonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

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

    private val internalMediaContentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val timestamp = readLastDateFromMediaStore(MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            Log.d("Internal Media has been changed uri=$uri timestamp=$timestamp")
        }
    }

    private val externalMediaContentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val timestamp = readLastDateFromMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            Log.d("External Media has been changed uri=$uri timestamp=$timestamp")
        }
    }

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

    private fun readLastDateFromMediaStore(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, "date_added DESC")!!
        var res: Long = -1
        if (cursor.moveToNext()) {
            res = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
        }
        cursor.close()
        return res
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    companion object {
        var isMonitoring: Boolean = false
    }
}