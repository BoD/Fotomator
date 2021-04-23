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
package org.jraf.android.fotomator.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import org.jraf.android.fotomator.R

private val COLORS_LIGHT
    @Composable
    get() = lightColors(
        primary = colorResource(R.color.colorPrimary),
        primaryVariant = colorResource(R.color.colorPrimary),
        secondary = colorResource(R.color.colorPrimary),
        secondaryVariant = colorResource(R.color.colorPrimary),
    )

private val COLORS_DARK
    @Composable
    get() = darkColors(
        primary = colorResource(R.color.colorPrimaryDark),
        primaryVariant = colorResource(R.color.colorPrimaryDark),
        secondary = colorResource(R.color.colorPrimaryDark),
        secondaryVariant = colorResource(R.color.colorPrimaryDark),
    )

@Composable
fun FotomatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) COLORS_DARK else COLORS_LIGHT,
        content = content
    )
}