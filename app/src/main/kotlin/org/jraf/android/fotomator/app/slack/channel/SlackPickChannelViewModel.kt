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
package org.jraf.android.fotomator.app.slack.channel

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.upload.client.SlackClient

class SlackPickChannelViewModel @ViewModelInject constructor(
    private val slackClient: SlackClient,
) : ViewModel() {

    val isLoadingVisible = MutableLiveData(true)
    val toast = MutableLiveData<Int?>()
    val finishWithError = MutableLiveData<Unit>()

    val channelList: LiveData<List<String>> = liveData {
        setLoading(true)
        val channelList = slackClient.getChannelList()
        setLoading(false)
        if (channelList == null) {
            showToast(R.string.slack_pick_channel_failed)
            finishWithError.value = Unit
        } else {
            emit(channelList)
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoadingVisible.value = loading
    }

    private fun showToast(@StringRes resId: Int) {
        toast.value = resId
        toast.value = null
    }
}