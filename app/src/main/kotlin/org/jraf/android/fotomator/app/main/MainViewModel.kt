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
package org.jraf.android.fotomator.app.main

import android.content.Context
import androidx.core.os.postDelayed
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.util.fireAndForget
import org.jraf.android.util.handler.HandlerUtil
import org.jraf.android.util.log.Log
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val prefs: AppPrefs
) : AndroidViewModel(context.applicationContext as android.app.Application) {
    val isServiceEnabledLiveData: MutableLiveData<Boolean> = prefs.isServiceEnabledLiveData

    val slackAuthToken: String? by prefs::slackAuthToken
    var slackChannel: String? by prefs::slackChannel
    val slackChannelLiveData: MutableLiveData<String?> = prefs.slackChannelLiveData

    val pickSlackChannel = MutableLiveData<Unit?>()
    val isAutomaticallyStopServiceDialogVisible = MutableLiveData(false)
    val showAutomaticallyStopServiceDatePicker = MutableLiveData<Unit?>()
    val showAutomaticallyStopServiceTimePicker = MutableLiveData<Unit?>()
    val automaticallyStopServiceDateIsInThePast = MutableLiveData<Unit?>()

    val automaticallyStopServiceDateTimeFormatted = prefs.automaticallyStopServiceDateTime.map { automaticallyStopServiceDateTime ->
        if (automaticallyStopServiceDateTime == null) {
            context.getString(R.string.main_automaticallyStopServiceDateTime_notSet)
        } else {
            val formattedDateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(automaticallyStopServiceDateTime))
            context.getString(R.string.main_automaticallyStopServiceDateTime_set, formattedDateTime)
        }
    }

    private var automaticallyStopServiceDatePicked: Long? = null

    fun onServiceEnabledSwitchClick() {
        isServiceEnabledLiveData.value = !isServiceEnabledLiveData.value!!
        val serviceEnabled = isServiceEnabledLiveData.value!!
        Log.d("serviceEnabled=$serviceEnabled")
        if (serviceEnabled) {
            // Wait a few milliseconds for the switch animation to have time to run
            HandlerUtil.getMainHandler().postDelayed(300L) {
                isAutomaticallyStopServiceDialogVisible.value = true
            }
        }
    }

    fun onChannelClick() {
        pickSlackChannel.fireAndForget()
    }

    fun onAutomaticallyStopServiceDialogSetDateTimeClick() {
        isAutomaticallyStopServiceDialogVisible.value = false
        showAutomaticallyStopServiceDatePicker.fireAndForget()
    }

    fun onAutomaticallyStopServiceDialogManuallyClick() {
        isAutomaticallyStopServiceDialogVisible.value = false
    }

    fun onAutomaticallyStopServiceDatePicked(timestamp: Long?) {
        showAutomaticallyStopServiceDatePicker.value = null
        automaticallyStopServiceDatePicked = timestamp
        if (timestamp == null) {
            prefs.automaticallyStopServiceDateTime.value = null
            return
        }
        showAutomaticallyStopServiceTimePicker.fireAndForget()
    }

    fun onAutomaticallyStopServiceTimePicked(hour: Int?, minute: Int?) {
        showAutomaticallyStopServiceTimePicker.value = null
        if (hour == null || minute == null) {
            prefs.automaticallyStopServiceDateTime.value = null
            return
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = automaticallyStopServiceDatePicked!!
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        Log.d("calendar=$calendar")
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            prefs.automaticallyStopServiceDateTime.value = null
            automaticallyStopServiceDateIsInThePast.fireAndForget()
        } else {
            prefs.automaticallyStopServiceDateTime.value = calendar.time.time
        }
    }

    fun onAutomaticallyStopServiceDateTimeClick() {
        showAutomaticallyStopServiceDatePicker.fireAndForget()
    }
}
