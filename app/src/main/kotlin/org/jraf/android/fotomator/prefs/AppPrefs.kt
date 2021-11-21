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
package org.jraf.android.fotomator.prefs

import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import org.jraf.android.kprefs.Key
import org.jraf.android.kprefs.Prefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = Prefs(context)

    var isServiceEnabled: Boolean by prefs.Boolean(false, KEY_IS_SERVICE_ENABLED)
    val isServiceEnabledLiveData: MutableLiveData<Boolean> by prefs.BooleanLiveData(false, KEY_IS_SERVICE_ENABLED)
    val automaticallyStopServiceDateTimeLiveData: MutableLiveData<Long?> by prefs.LongLiveData(KEY_AUTOMATICALLY_STOP_SERVICE_DATE_TIME)

    var slackAuthToken: String? by prefs.String()

    var slackTeamName: String? by prefs.String(KEY_SLACK_TEAM_NAME)

    var slackChannelName: String? by prefs.String(KEY_SLACK_CHANNEL_NAME)
    var slackChannelId: String? by prefs.String(KEY_SLACK_CHANNEL_ID)

    val isServiceEnabledFlow: MutableStateFlow<Boolean> by prefs.BooleanFlow(false, KEY_IS_SERVICE_ENABLED)
    val automaticallyStopServiceDateTimeFlow: MutableStateFlow<Long?> by prefs.LongFlow(KEY_AUTOMATICALLY_STOP_SERVICE_DATE_TIME)
    val slackTeamNameFlow: MutableStateFlow<String?> by prefs.StringFlow(KEY_SLACK_TEAM_NAME)
    val slackChannelNameFlow: MutableStateFlow<String?> by prefs.StringFlow(KEY_SLACK_CHANNEL_NAME)

    var isFirstRun: Boolean by prefs.Boolean(default = true)

    companion object {
        private val KEY_IS_SERVICE_ENABLED = Key("isServiceEnabled")
        private val KEY_AUTOMATICALLY_STOP_SERVICE_DATE_TIME = Key("automaticallyStopServiceDateTime")
        private val KEY_SLACK_CHANNEL_NAME = Key("slackChannel")
        private val KEY_SLACK_CHANNEL_ID = Key("slackChannelId")
        private val KEY_SLACK_TEAM_NAME = Key("slackTeamName")

    }
}
