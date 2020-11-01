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
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.util.log.Log
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class MainViewModel @ViewModelInject constructor(
    @ApplicationContext context: Context,
    private val prefs: AppPrefs
) : AndroidViewModel(context.applicationContext as android.app.Application) {
    val isServiceEnabledLiveData: MutableLiveData<Boolean> = prefs.isServiceEnabledLiveData

    val slackAuthToken: String? by prefs::slackAuthToken
    var slackChannel: String? by prefs::slackChannel
    val slackChannelLiveData: MutableLiveData<String?> = prefs.slackChannelLiveData

    val pickSlackChannel = MutableLiveData<Unit?>()
    val showAutomaticallyStopServiceDialog = MutableLiveData<Unit?>()
    val showAutomaticallyStopServiceDatePicker = MutableLiveData<Unit?>()
    val showAutomaticallyStopServiceTimePicker = MutableLiveData<Unit?>()

    val automaticallyStopServiceDateTimeFormatted = prefs.automaticallyStopServiceDateTime.map { automaticallyStopServiceDateTime ->
        if (automaticallyStopServiceDateTime == null) {
            context.getString(R.string.main_automaticallyStopServiceDateTime_notSet)
        } else {
            val formattedDateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(automaticallyStopServiceDateTime))
            context.getString(R.string.main_automaticallyStopServiceDateTime_set, formattedDateTime)
        }
    }

    private var automaticallyStopServiceDatePicked: Long? = null

    fun onServiceEnabledSwitchClick(isChecked: Boolean) {
        Log.d("isChecked=$isChecked")

        if (isChecked) {
            showAutomaticallyStopServiceDialog.value = Unit
            showAutomaticallyStopServiceDialog.value = null
        }
    }

    fun onChannelClick() {
        pickSlackChannel.value = Unit
        pickSlackChannel.value = null
    }

    fun onAutomaticallyStopServiceDatePicked(timestamp: Long?) {
        showAutomaticallyStopServiceDatePicker.value = null
        automaticallyStopServiceDatePicked = timestamp
        if (timestamp == null) {
            prefs.automaticallyStopServiceDateTime.value = null
            return
        }
        showAutomaticallyStopServiceTimePicker.value = Unit
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
        prefs.automaticallyStopServiceDateTime.value = calendar.time.time
    }

    fun onAutomaticallyStopServiceDateTimeClick() {
        showAutomaticallyStopServiceDatePicker.value = Unit
    }
}
