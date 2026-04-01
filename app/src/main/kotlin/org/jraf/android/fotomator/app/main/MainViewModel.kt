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
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.main.MainLayoutState.DialogType
import org.jraf.android.fotomator.configure.Configure
import org.jraf.android.fotomator.configure.isConfigureIntent
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.util.logd
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPrefs,
) : AndroidViewModel(context.applicationContext as android.app.Application) {
    val isServiceEnabled: MutableStateFlow<Boolean> = prefs.isServiceEnabledFlow

    val slackAuthToken: String? by prefs::slackAuthToken
    var slackChannelName: String? by prefs::slackChannelName
    var slackChannelId: String? by prefs::slackChannelId

    private val shownDialog = MutableStateFlow(DialogType.NONE)
    val automaticallyStopServiceDateIsInThePast = MutableSharedFlow<Unit>()
    val setupSlackAuth = MutableSharedFlow<Unit>()

    val layoutState: StateFlow<MainLayoutState> = combine(
        prefs.isServiceEnabledFlow,
        prefs.automaticallyStopServiceDateTimeFlow,
        prefs.slackTeamNameFlow,
        prefs.slackChannelNameFlow,
        shownDialog,
    ) { isServiceEnabled, automaticallyStopServiceDateTime, slackTeamName, slackChannel, shownDialog ->
        val automaticallyStopServiceDateTimeFormatted = formatAutomaticallyStopServiceDateTime(automaticallyStopServiceDateTime)
        MainLayoutState(
            isServiceEnabled = isServiceEnabled,
            slackTeamName = slackTeamName,
            slackChannelName = slackChannel,
            automaticallyStopServiceDateTimeFormatted = automaticallyStopServiceDateTimeFormatted,
            shownDialog = shownDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = MainLayoutState(
            isServiceEnabled = false,
            slackTeamName = null,
            slackChannelName = null,
            automaticallyStopServiceDateTimeFormatted = formatAutomaticallyStopServiceDateTime(prefs.automaticallyStopServiceDateTimeFlow.value),
            shownDialog = DialogType.NONE
        ),
    )

    private fun formatAutomaticallyStopServiceDateTime(automaticallyStopServiceDateTime: Long?) = if (automaticallyStopServiceDateTime == null) {
        context.getString(R.string.main_automaticallyStopServiceDateTime_notSet)
    } else {
        val formattedDateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(automaticallyStopServiceDateTime))
        context.getString(R.string.main_automaticallyStopServiceDateTime_set, formattedDateTime)
    }

    private var automaticallyStopServiceDatePicked: Long? = null
    private var automaticallyStopServiceDateTimeSetupByConfigureLink = false

    fun onServiceEnabledSwitchClick() {
        isServiceEnabled.value = !isServiceEnabled.value
        val serviceEnabled = isServiceEnabled.value
        logd("serviceEnabled=$serviceEnabled")
        if (serviceEnabled && !automaticallyStopServiceDateTimeSetupByConfigureLink) {
            // Wait a few milliseconds for the switch animation to have time to run
            viewModelScope.launch {
                delay(300L)
                shownDialog.value = DialogType.AUTOMATICALLY_STOP_SERVICE_CHOICE
            }
        }
        automaticallyStopServiceDateTimeSetupByConfigureLink = false
    }

    fun onAutomaticallyStopServiceChoiceDialogSetDateTimeClick() {
        shownDialog.value = DialogType.AUTOMATICALLY_STOP_SERVICE_DATE
    }

    fun onAutomaticallyStopServiceChoiceDialogManuallyClick() {
        shownDialog.value = DialogType.NONE
    }

    fun onAutomaticallyStopServiceDatePicked(timestamp: Long?) {
        automaticallyStopServiceDatePicked = timestamp
        if (timestamp == null) {
            prefs.automaticallyStopServiceDateTimeFlow.value = null
            return
        }
        shownDialog.value = DialogType.AUTOMATICALLY_STOP_SERVICE_TIME
    }

    fun onAutomaticallyStopServiceTimePicked(hour: Int?, minute: Int?) {
        shownDialog.value = DialogType.NONE
        if (hour == null || minute == null) {
            prefs.automaticallyStopServiceDateTimeFlow.value = null
            return
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = automaticallyStopServiceDatePicked!!
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        logd("calendar=$calendar")
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            prefs.automaticallyStopServiceDateTimeFlow.value = null
            automaticallyStopServiceDateIsInThePast.tryEmit(Unit)
        } else {
            prefs.automaticallyStopServiceDateTimeFlow.value = calendar.time.time
        }
    }

    fun onAutomaticallyStopServiceDateTimeClick() {
        shownDialog.value = DialogType.AUTOMATICALLY_STOP_SERVICE_CHOICE
    }

    fun onDisconnectSlackClick() {
        prefs.isServiceEnabled = false
        prefs.slackChannelName = null
        prefs.slackChannelId = null
        prefs.slackTeamName = null
        prefs.slackAuthToken = null
        setupSlackAuth.tryEmit(Unit)
    }

    fun handleConfigureIntent(intent: Intent) {
        val isConfigureIntent = isConfigureIntent(intent)
        logd("isConfigureIntent=$isConfigureIntent")

        if (isConfigureIntent) {
            val configure = Configure.fromIntent(intent)
            logd("configure=$configure")

            prefs.slackChannelId = configure.channel
            prefs.automaticallyStopServiceDateTimeFlow.value = configure.automaticallyStopServiceDateTime

            if (configure.automaticallyStopServiceDateTime != null) {
                automaticallyStopServiceDateTimeSetupByConfigureLink = true
            }
        }
    }
}
