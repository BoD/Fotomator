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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
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
import org.jraf.android.fotomator.upload.client.slack.SlackChannelOrConversation
import org.jraf.android.fotomator.upload.client.slack.SlackGroupConversation
import org.jraf.android.fotomator.upload.client.slack.SlackSingleConversation
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlackPickChannelLayout(
    state: SlackPickChannelLayoutState,
    onBackClick: () -> Unit,
    onChannelClick: (SlackChannelOrConversation) -> Unit,
    onSearchQueryChange: (String) -> Unit,
) {
    FotomatorTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.slack_pick_channel_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.slack_pick_channel_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                            )

                        }
                    },
                    colors = smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                )
            },
            content = {
                SlackPickChannelContent(
                    state,
                    onChannelClick,
                    onSearchQueryChange
                )
            }
        )
    }
}

@Composable
private fun SlackPickChannelContent(
    state: SlackPickChannelLayoutState,
    onChannelClick: (SlackChannelOrConversation) -> Unit,
    onSearchQueryChange: (String) -> Unit,
) {
    Crossfade(state is SlackPickChannelLayoutState.Loading) { isLoading ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(
                    // TODO Remove this when CircularProgressIndicator exists in Material 3
                    color = MaterialTheme.colorScheme.primary
                )
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

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelList(
    channelList: List<SlackChannelOrConversation>,
    onChannelClick: (SlackChannelOrConversation) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        val channelListByType: Map<KClass<out SlackChannelOrConversation>, List<SlackChannelOrConversation>> = channelList.groupBy { it::class }

        val channels = channelListByType[SlackChannel::class] as? List<SlackChannel>
        if (channels?.isNotEmpty() == true) {
            stickyHeader {
                Separator(stringResource(R.string.slack_pick_channel_channels))
            }
            items(channels) { channel ->
                ChannelRow(channel, onClick = {
                    onChannelClick(channel)
                })
            }
        }

        val groupConversations = channelListByType[SlackGroupConversation::class] as? List<SlackGroupConversation>
        if (groupConversations?.isNotEmpty() == true) {
            stickyHeader {
                Separator(stringResource(R.string.slack_pick_channel_groupMessaging))
            }
            items(groupConversations) { channel ->
                GroupConversationRow(channel, onClick = {
                    onChannelClick(channel)
                })
            }
        }

        val privateConversations = channelListByType[SlackSingleConversation::class] as? List<SlackSingleConversation>
        if (privateConversations?.isNotEmpty() == true) {
            stickyHeader {
                Separator(stringResource(R.string.slack_pick_channel_privateMessaging))
            }
            items(privateConversations) { channel ->
                SingleConversationRow(channel, onClick = {
                    onChannelClick(channel)
                })
            }
        }
    }
}

@Composable
private fun Separator(title: String) {
    Text(title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.inverseSurface)
            .padding(vertical = 4.dp, horizontal = 16.dp)
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ChannelRow(channel: SlackChannel, onClick: () -> Unit) = ListItem(
    // TODO: Keeping androidx.compose.material.Text here for now, with a hardcoded color,
    //  so the styles inherited from ListItem work correctly
    text = {
        androidx.compose.material.Text(
            "# ${channel.name}",
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    secondaryText = when {
        channel.topic != null -> {
            {
                androidx.compose.material.Text(
                    channel.topic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        channel.purpose != null -> {
            {
                androidx.compose.material.Text(
                    channel.purpose,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        else -> null
    },
    modifier = Modifier.clickable(onClick = onClick),
)

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun SingleConversationRow(conversation: SlackSingleConversation, onClick: () -> Unit) = ListItem(
    // TODO: Keeping androidx.compose.material.Text here for now, with a hardcoded color,
    //  so the styles inherited from ListItem work correctly
    text = {
        androidx.compose.material.Text(
            conversation.description,
            color = MaterialTheme.colorScheme.onSurface
        )
    },

    modifier = Modifier.clickable(onClick = onClick),
)

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun GroupConversationRow(conversation: SlackGroupConversation, onClick: () -> Unit) = ListItem(
    // TODO: Keeping androidx.compose.material.Text here for now, with a hardcoded color,
    //  so the styles inherited from ListItem work correctly
    text = {
        androidx.compose.material.Text(
            stringResource(R.string.slack_pick_channel_groupMessaging),
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    secondaryText = {
        androidx.compose.material.Text(
            conversation.description,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    modifier = Modifier.clickable(onClick = onClick),
)


@Preview
@Composable
private fun SlackPickChannelLayoutNotLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loading,
        onBackClick = {},
        onChannelClick = {},
        onSearchQueryChange = {}
    )
}

@Preview
@Composable
private fun SlackPickChannelLayoutLoadingPreview() {
    SlackPickChannelLayout(
        state = SlackPickChannelLayoutState.Loaded(
            channelList = listOf(
                SlackChannel(id = "a", name = "abcd", topic = "This channel is for fun and or work", purpose = "This channel has a purpose"),
                SlackChannel(id = "b", name = "test", topic = "I have a topic!", purpose = null),
                SlackChannel(id = "c", name = "android", topic = null, purpose = "I have no purpose in life"),
                SlackChannel(id = "d", name = "why-not", topic = null, purpose = null),
                SlackSingleConversation(id = "e", description = "John Doe"),
                SlackSingleConversation(id = "f", description = "Jane Smith"),
                SlackGroupConversation(id = "f", description = "Group messaging with: @bod @nounours.bear @doupidou"),
            )
                    + List(42) { SlackChannel("id", "channel$it", null, null) },
            searchQuery = "Test"
        ),
        onBackClick = {},
        onChannelClick = {},
        onSearchQueryChange = {}
    )
}

sealed class SlackPickChannelLayoutState {
    object Loading : SlackPickChannelLayoutState()
    data class Loaded(
        val channelList: List<SlackChannelOrConversation>,
        val searchQuery: String?,
    ) : SlackPickChannelLayoutState()
}
