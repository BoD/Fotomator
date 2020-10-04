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
import org.jraf.android.fotomator.upload.SlackClient
import org.jraf.android.util.log.Log
import java.io.FileInputStream

class PhotoMonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val slackClient = SlackClient()

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
        val parcelFileDescriptor = contentResolver.openFileDescriptor(mediaContentUri, "r")
        if (parcelFileDescriptor == null) {
            Log.w("parcelFileDescriptor is null, give up")
            return
        }
        GlobalScope.launch {
            parcelFileDescriptor.use {
                val ok = slackClient.uploadFile(fileInputStream = FileInputStream(it.fileDescriptor), "test")
                Log.d("ok=$ok")
            }
        }

//        val inputStream = contentResolver.openInputStream(mediaContentUri)
//        if (inputStream == null) {
//            Log.w("inputStream is null, give up")
//            return
//        }
//        inputStream.use {
//            GlobalScope.launch {
//                val ok = slackClient.uploadFile(fileInputStream = it, "test")
//                Log.d("ok=$ok")
//            }
//        }

    }

    companion object {
        var isMonitoring: Boolean = false
    }
}