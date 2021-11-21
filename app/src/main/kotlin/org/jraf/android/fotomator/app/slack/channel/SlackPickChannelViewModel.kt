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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.upload.client.slack.SlackChannel
import org.jraf.android.fotomator.upload.client.slack.SlackChannelOrConversation
import org.jraf.android.fotomator.upload.client.slack.SlackClient
import org.jraf.android.fotomator.upload.client.slack.SlackConversation
import org.jraf.android.fotomator.util.fireAndForget
import javax.inject.Inject

@HiltViewModel
class SlackPickChannelViewModel @Inject constructor(
    private val slackClient: SlackClient,
) : ViewModel() {

    val toast = MutableLiveData<Int?>()
    val finishWithError = MutableLiveData<Unit>()

    private var channelList: List<SlackChannelOrConversation>? = null

    val layoutState = MutableLiveData<SlackPickChannelLayoutState>(SlackPickChannelLayoutState.Loading)

    init {
        viewModelScope.launch {
            val channelList = slackClient.getChannelList()
            this@SlackPickChannelViewModel.channelList = channelList
            if (channelList == null) {
                showToast(R.string.slack_pick_channel_failed)
                finishWithError.value = Unit
            } else {
                layoutState.value = SlackPickChannelLayoutState.Loaded(channelList, null)
            }
        }
    }

    private fun showToast(@StringRes resId: Int) {
        toast.fireAndForget(resId)
    }

    fun updateSearchQuery(query: String) {
        val trimmedQuery = query.trim()
        layoutState.value = SlackPickChannelLayoutState.Loaded(channelList!!.filter { channel ->
            channel is SlackChannel && (
                    channel.name.contains(trimmedQuery, ignoreCase = true) ||
                            channel.purpose?.contains(trimmedQuery, ignoreCase = true) == true ||
                            channel.topic?.contains(trimmedQuery, ignoreCase = true) == true
                    ) || channel is SlackConversation && channel.description.contains(trimmedQuery, ignoreCase = true)
        }, query)
    }
}
