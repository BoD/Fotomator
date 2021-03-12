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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jraf.android.fotomator.theme.FotomatorTheme

@Composable
fun SlackPickChannelLayout(
    state: SlackPickChannelLayoutState,
    onChannelClick: (String) -> Unit,
) {
    FotomatorTheme {
        Crossfade(state is SlackPickChannelLayoutState.Loading) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize(),
                ) {
                    items((state as SlackPickChannelLayoutState.Loaded).channelList) { channel ->
                        ChannelRow(channel, onClick = {
                            onChannelClick(channel)
                        })
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ChannelRow(channel: String, onClick: () -> Unit) = Surface {
    ListItem(
        text = {
            Text("# $channel")
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview
@Composable
fun SlackPickChannelLayoutNotLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loading,
        onChannelClick = {}
    )
}

@Preview
@Composable
fun SlackPickChannelLayoutLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loaded(
            listOf("abcd", "test", "android")
                    + List(42) { "channel$it" }
        ),
        onChannelClick = {}
    )
}

sealed class SlackPickChannelLayoutState {
    object Loading : SlackPickChannelLayoutState()
    data class Loaded(val channelList: List<String>) : SlackPickChannelLayoutState()
}
