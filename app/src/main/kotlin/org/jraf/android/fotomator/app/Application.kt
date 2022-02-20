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
package org.jraf.android.fotomator.app

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import org.jraf.android.util.log.Log

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Log
        Log.init(this, LOG_TAG, true)

        // Material dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Change the status bar color (for some reason, this is needed on Samsung)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                    // Do not touch external activities (e.g. About screen)
                    if (!activity::class.java.name.startsWith("org.jraf.android.fotomator")) return

                    val isNightMode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    activity.window.statusBarColor =
                        activity.getColor(if (isNightMode) android.R.color.system_accent1_700 else android.R.color.system_accent1_100)
                    activity.window.navigationBarColor =
                        activity.getColor(if (isNightMode) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_10)
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })
        }
    }

    companion object {
        private const val LOG_TAG = "Fotomator"
    }
}
