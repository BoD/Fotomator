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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.theme.FotomatorTheme

@Composable
fun MainLayout(
    isServiceEnabled: Boolean,
    slackChannel: String?,
    automaticallyStopServiceDateTimeFormatted: String,
    onServiceEnabledClick: () -> Unit,
    onAboutClick: () -> Unit,
    onChannelClick: () -> Unit,
    onAutomaticallyStopServiceDateTimeClick: () -> Unit,
) {
    FotomatorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(R.drawable.logo_full_white),
                            contentDescription = null
                        )
                    },
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
                    isServiceEnabled,
                    slackChannel,
                    automaticallyStopServiceDateTimeFormatted,
                    onServiceEnabledClick,
                    onChannelClick,
                    onAutomaticallyStopServiceDateTimeClick,
                )
            }
        )
    }
}

@Composable
private fun MainContent(
    isServiceEnabled: Boolean,
    slackChannel: String?,
    automaticallyStopServiceDateTimeFormatted: String,
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onServiceEnabledClick)
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(if (isServiceEnabled) R.string.main_service_switch_enabled else R.string.main_service_switch_disabled),
                    style = MaterialTheme.typography.body1,
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = null,
                )
            }

            if (slackChannel != null) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onChannelClick, enabled = isServiceEnabled) {
                    Text(stringResource(R.string.main_channel, slackChannel), letterSpacing = 0.sp)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onAutomaticallyStopServiceDateTimeClick, enabled = isServiceEnabled) {
                    Text(automaticallyStopServiceDateTimeFormatted, letterSpacing = 0.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun MainLayoutPreview() {
    MainLayout(
        isServiceEnabled = true,
        slackChannel = "test",
        automaticallyStopServiceDateTimeFormatted = "Stop on Oct. 11 at 1:30 PM",
        onServiceEnabledClick = {},
        onAboutClick = {},
        onChannelClick = {},
        onAutomaticallyStopServiceDateTimeClick = {},
    )
}