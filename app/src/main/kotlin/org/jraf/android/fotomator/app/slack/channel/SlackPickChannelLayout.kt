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
@file:OptIn(ExperimentalAnimationApi::class)

package org.jraf.android.fotomator.app.slack.channel

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.theme.FotomatorTheme
import org.jraf.android.fotomator.upload.client.slack.SlackChannel

@Composable
fun SlackPickChannelLayout(
    state: SlackPickChannelLayoutState,
    onChannelClick: (SlackChannel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
) {
    FotomatorTheme {
        Crossfade(state is SlackPickChannelLayoutState.Loading) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                state as SlackPickChannelLayoutState.Loaded
                Surface {
                    Column(Modifier.fillMaxSize()) {
                        SearchTextField(
                            searchQuery = state.searchQuery,
                            onQueryChange = onSearchQueryChange,
                        )

                        ChannelList(
                            channelList = state.channelList,
                            onChannelClick = onChannelClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String?,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        value = searchQuery ?: "",
        singleLine = true,
        onValueChange = onQueryChange,
        placeholder = {
            Text(text = stringResource(R.string.slack_pick_channel_search))
        }
    )
}

@Composable
private fun ChannelList(
    channelList: List<SlackChannel>,
    onChannelClick: (SlackChannel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(channelList) { channel ->
            ChannelRow(channel, onClick = {
                onChannelClick(channel)
            })
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ChannelRow(channel: SlackChannel, onClick: () -> Unit) = ListItem(
    text = {
        Text("# ${channel.name}")
    },
    secondaryText = when {
        channel.topic != null -> {
            { Text(channel.topic, maxLines = 3, overflow = TextOverflow.Ellipsis) }
        }
        channel.purpose != null -> {
            { Text(channel.purpose, maxLines = 3, overflow = TextOverflow.Ellipsis) }
        }
        else -> null
    },
    modifier = Modifier.clickable(onClick = onClick),
)


@Preview
@Composable
fun SlackPickChannelLayoutNotLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loading,
        onChannelClick = {},
        onSearchQueryChange = {}
    )
}

@Preview
@Composable
fun SlackPickChannelLayoutLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loaded(
            channelList = listOf(
                SlackChannel(name = "abcd", topic = "This channel is for fun and or work", purpose = "This channel has a purpose"),
                SlackChannel(name = "test", topic = "I have a topic!", purpose = null),
                SlackChannel(name = "android", topic = null, purpose = "I have no purpose in life"),
                SlackChannel(name = "why-not", topic = null, purpose = null),
            )
                    + List(42) { SlackChannel("channel$it", null, null) },
            searchQuery = "Test"
        ),
        onChannelClick = {},
        onSearchQueryChange = {}
    )
}

sealed class SlackPickChannelLayoutState {
    object Loading : SlackPickChannelLayoutState()
    data class Loaded(
        val channelList: List<SlackChannel>,
        val searchQuery: String?,
    ) : SlackPickChannelLayoutState()
}
