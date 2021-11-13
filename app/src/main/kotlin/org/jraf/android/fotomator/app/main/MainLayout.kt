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
package org.jraf.android.fotomator.app.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.theme.FotomatorTheme

data class MainLayoutState(
    val isServiceEnabled: Boolean,
    val slackTeamName: String?,
    val slackChannel: String?,
    val automaticallyStopServiceDateTimeFormatted: String,
    val isAutomaticallyStopServiceDialogVisible: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    state: MainLayoutState,
    onServiceEnabledClick: () -> Unit,
    onDisconnectSlackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onChannelClick: () -> Unit,
    onAutomaticallyStopServiceDateTimeClick: () -> Unit,
    onAutomaticallyStopServiceDialogSetDateTimeClick: () -> Unit,
    onAutomaticallyStopServiceDialogManuallyClick: () -> Unit,
) {
    FotomatorTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(R.drawable.logo_full_white),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    colors = centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.main_menu_more))
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                onDisconnectSlackClick()
                            }) {
                                Text(stringResource(R.string.main_menu_disconnectSlack))
                            }
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                onAboutClick()
                            }) {
                                Text(stringResource(R.string.main_menu_about))
                            }
                        }
                    },
                )
            },
            content = {
                MainContent(
                    state,
                    onServiceEnabledClick,
                    onChannelClick,
                    onAutomaticallyStopServiceDateTimeClick,
                )

                if (state.isAutomaticallyStopServiceDialogVisible) {
                    AutomaticallyStopServiceDialog(onAutomaticallyStopServiceDialogManuallyClick, onAutomaticallyStopServiceDialogSetDateTimeClick)
                }
            }
        )
    }
}

@Composable
private fun MainContent(
    state: MainLayoutState,
    onServiceEnabledClick: () -> Unit,
    onChannelClick: () -> Unit,
    onAutomaticallyStopServiceDateTimeClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .clip(androidx.compose.material.MaterialTheme.shapes.small)
                    .clickable(onClick = onServiceEnabledClick)
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(if (state.isServiceEnabled) R.string.main_service_switch_enabled else R.string.main_service_switch_disabled),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.isServiceEnabled,
                    onCheckedChange = null,
                    modifier = Modifier.alignByBaseline(),

                    // TODO Necessary for now with Material 3 alpha, but should probably work by default
                    // with later versions
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            }

            if (state.slackChannel != null) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onChannelClick, enabled = state.isServiceEnabled) {
                    Text(stringResource(R.string.main_channel, "${state.slackTeamName} #${state.slackChannel}"), letterSpacing = 0.sp)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onAutomaticallyStopServiceDateTimeClick, enabled = state.isServiceEnabled) {
                    Text(state.automaticallyStopServiceDateTimeFormatted, letterSpacing = 0.sp)
                }
            }
        }
    }
}

@Composable
private fun AutomaticallyStopServiceDialog(
    onAutomaticallyStopServiceDialogManuallyClick: () -> Unit,
    onAutomaticallyStopServiceDialogSetDateTimeClick: () -> Unit,
) = AlertDialog(
    onDismissRequest = onAutomaticallyStopServiceDialogManuallyClick,
    title = {
        Text(stringResource(R.string.main_automaticallyStopServiceDialog_title))
    },
    text = {
        Text(stringResource(R.string.main_automaticallyStopServiceDialog_message))
    },
    confirmButton = {
        TextButton(onClick = onAutomaticallyStopServiceDialogSetDateTimeClick) {
            Text(stringResource(R.string.main_automaticallyStopServiceDialog_positive))
        }
    },
    dismissButton = {
        TextButton(onClick = onAutomaticallyStopServiceDialogManuallyClick) {
            Text(stringResource(R.string.main_automaticallyStopServiceDialog_negative))
        }
    }
)

@Preview
@Composable
private fun MainLayoutPreview() {
    MainLayout(
        state = MainLayoutState(
            isServiceEnabled = true,
            slackTeamName = "BoD, inc.",
            slackChannel = "test",
            automaticallyStopServiceDateTimeFormatted = "Stop on Oct. 11 at 1:30 PM",
            isAutomaticallyStopServiceDialogVisible = false
        ),
        onServiceEnabledClick = {},
        onDisconnectSlackClick = {},
        onAboutClick = {},
        onChannelClick = {},
        onAutomaticallyStopServiceDateTimeClick = {},
        onAutomaticallyStopServiceDialogSetDateTimeClick = {},
        onAutomaticallyStopServiceDialogManuallyClick = {},
    )
}

@Preview
@Composable
private fun AutomaticallyStopServiceDialogPreview() = AutomaticallyStopServiceDialog({}, {})