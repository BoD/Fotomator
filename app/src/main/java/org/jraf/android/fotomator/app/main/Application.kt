package org.jraf.android.fotomator.app.main

import android.app.Application
import org.jraf.android.fotomator.BuildConfig
import org.jraf.android.util.log.Log

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Log
        Log.init(this, LOG_TAG, BuildConfig.DEBUG)
    }

    companion object {
        private const val LOG_TAG = "Fotomator"
    }
}