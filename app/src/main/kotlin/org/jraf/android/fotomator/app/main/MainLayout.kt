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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.main.MainLayoutState.DialogType
import org.jraf.android.fotomator.theme.FotomatorTheme

data class MainLayoutState(
    val isServiceEnabled: Boolean,
    val slackTeamName: String?,
    val slackChannelName: String?,
    val automaticallyStopServiceDateTimeFormatted: String,
    val shownDialog: DialogType,
) {
    enum class DialogType {
        NONE,
        AUTOMATICALLY_STOP_SERVICE_CHOICE,
        AUTOMATICALLY_STOP_SERVICE_DATE,
        AUTOMATICALLY_STOP_SERVICE_TIME,
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    state: MainLayoutState,
    onServiceEnabledClick: () -> Unit,
    onDisconnectSlackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onChannelClick: () -> Unit,
    onAutomaticallyStopServiceDateTimeClick: () -> Unit,
    onAutomaticallyStopServiceChoiceDialogSetDateTimeClick: () -> Unit,
    onAutomaticallyStopServiceChoiceDialogManuallyClick: () -> Unit,
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
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.main_menu_disconnectSlack))
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDisconnectSlackClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.main_menu_about))
                                },
                                onClick = {
                                    menuExpanded = false
                                    onAboutClick()
                                },
                            )
                        }
                    },
                )
            },
            content = { paddingValues ->
                MainContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = state,
                    onServiceEnabledClick = onServiceEnabledClick,
                    onChannelClick = onChannelClick,
                    onAutomaticallyStopServiceDateTimeClick = onAutomaticallyStopServiceDateTimeClick,
                )

                if (state.shownDialog == DialogType.AUTOMATICALLY_STOP_SERVICE_CHOICE) {
                    AutomaticallyStopServiceChoiceDialog(onAutomaticallyStopServiceChoiceDialogManuallyClick, onAutomaticallyStopServiceChoiceDialogSetDateTimeClick)
                }
            }
        )
    }
}

@Composable
private fun MainContent(
    modifier: Modifier,
    state: MainLayoutState,
    onServiceEnabledClick: () -> Unit,
    onChannelClick: () -> Unit,
    onAutomaticallyStopServiceDateTimeClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(horizontal = 16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnOffButton(state, onServiceEnabledClick)

            Spacer(Modifier.height(8.dp))
            val alpha: Float by animateFloatAsState(if (state.isServiceEnabled) 1F else 0F, label = "alpha")

            Blink(overallAlpha = alpha) {
                Text(
                    stringResource(R.string.main_monitoringPhotos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (state.slackChannelName != null) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onChannelClick, enabled = state.isServiceEnabled) {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.main_channel) + " ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(state.slackTeamName ?: "")
                            }
                            append(" / ")
                            append(state.slackChannelName)
                        })
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onAutomaticallyStopServiceDateTimeClick, enabled = state.isServiceEnabled) {
                    Text(state.automaticallyStopServiceDateTimeFormatted)
                }
            }
        }
    }
}

@Composable
private fun OnOffButton(state: MainLayoutState, onServiceEnabledClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
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

//            // TODO Necessary for now with Material 3 alpha, but should probably work by default
//            // with later versions
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = MaterialTheme.colorScheme.primary,
//                uncheckedThumbColor = MaterialTheme.colorScheme.primaryContainer,
//                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
//            )
        )
    }
}

@Composable
private fun AutomaticallyStopServiceChoiceDialog(
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

@Composable
private fun Blink(overallAlpha: Float = 1F, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition("blink")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1F,
        targetValue = .33F,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Box(modifier = Modifier.alpha(overallAlpha * alpha)) {
        content()
    }
}

@Preview
@Composable
private fun MainLayoutPreview() {
    MainLayout(
        state = MainLayoutState(
            isServiceEnabled = true,
            slackTeamName = "BoD, inc.",
            slackChannelName = "test",
            automaticallyStopServiceDateTimeFormatted = "Stop on Oct. 11 at 1:30 PM",
            shownDialog = DialogType.NONE,
        ),
        onServiceEnabledClick = {},
        onDisconnectSlackClick = {},
        onAboutClick = {},
        onChannelClick = {},
        onAutomaticallyStopServiceDateTimeClick = {},
        onAutomaticallyStopServiceChoiceDialogSetDateTimeClick = {},
        onAutomaticallyStopServiceChoiceDialogManuallyClick = {},
    )
}

@Preview
@Composable
private fun AutomaticallyStopServiceDialogPreview() = AutomaticallyStopServiceChoiceDialog({}, {})
