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

package org.jraf.android.fotomator.app.slack.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.theme.FotomatorTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlackAuthLayout(
    isLoading: Boolean,
    onStartAuthenticationClick: () -> Unit,
) {
    FotomatorTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(stringResource(R.string.slack_auth_title)) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                )
            },
            content = {
                SlackAuthContent(
                    isLoading,
                    onStartAuthenticationClick,
                )
            }
        )
    }
}

@Composable
fun SlackAuthContent(
    isLoading: Boolean,
    onStartAuthenticationClick: () -> Unit,
) {
    // TODO The Crossfade is on the whole screen (Box fillMaxSize twice), instead of its contents
    // TODO otherwise there's a misalignment happening when fading - not sure why.
    Crossfade(isLoading) { isLoading ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Button(onStartAuthenticationClick) {
                    Text(stringResource(R.string.slack_auth_button))
                }
            }
        }
    }
}


@Preview
@Composable
private fun SlackAuthLayoutNotLoadingPreview() {
    SlackAuthLayout(
        isLoading = false,
        onStartAuthenticationClick = {}
    )
}

@Preview
@Composable
private fun SlackAuthLayoutLoadingPreview() {
    SlackAuthLayout(
        isLoading = true,
        onStartAuthenticationClick = {}
    )
}