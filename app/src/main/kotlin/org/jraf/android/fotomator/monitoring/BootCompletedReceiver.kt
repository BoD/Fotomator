/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.util.log.Log
import org.jraf.android.util.string.StringUtil
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appPrefs: AppPrefs

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("intent=${StringUtil.toString(intent)}")
        startPhotoMonitoringService(context)
    }

    private fun startPhotoMonitoringService(context: Context) {
        if (appPrefs.isServiceEnabled && !PhotoMonitoringService.isStarted) {
            val serviceIntent = Intent(context, PhotoMonitoringService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}