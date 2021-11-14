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

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import org.jraf.android.fotomator.R

// Material 2

private val LightColors
    @Composable
    get() = lightColors(
        primary = colorResource(R.color.md_theme_light_primary),
        primaryVariant = colorResource(R.color.md_theme_light_primary),
        secondary = colorResource(R.color.md_theme_light_secondary),
        secondaryVariant = colorResource(R.color.md_theme_light_secondary),
    )

private val DarkColors
    @Composable
    get() = darkColors(
        primary = colorResource(R.color.md_theme_dark_primary),
        primaryVariant = colorResource(R.color.md_theme_dark_primary),
        secondary = colorResource(R.color.md_theme_dark_secondary),
        secondaryVariant = colorResource(R.color.md_theme_dark_secondary),
    )


// Material 3

private val LightColorScheme
    @Composable
    get() = lightColorScheme(
        primary = colorResource(R.color.md_theme_light_primary),
        onPrimary = colorResource(R.color.md_theme_light_onPrimary),
        primaryContainer = colorResource(R.color.md_theme_light_primaryContainer),
        onPrimaryContainer = colorResource(R.color.md_theme_light_onPrimaryContainer),
        secondary = colorResource(R.color.md_theme_light_secondary),
        onSecondary = colorResource(R.color.md_theme_light_onSecondary),
        secondaryContainer = colorResource(R.color.md_theme_light_secondaryContainer),
        onSecondaryContainer = colorResource(R.color.md_theme_light_onSecondaryContainer),
        tertiary = colorResource(R.color.md_theme_light_tertiary),
        onTertiary = colorResource(R.color.md_theme_light_onTertiary),
        tertiaryContainer = colorResource(R.color.md_theme_light_tertiaryContainer),
        onTertiaryContainer = colorResource(R.color.md_theme_light_onTertiaryContainer),
        error = colorResource(R.color.md_theme_light_error),
        errorContainer = colorResource(R.color.md_theme_light_errorContainer),
        onError = colorResource(R.color.md_theme_light_onError),
        onErrorContainer = colorResource(R.color.md_theme_light_onErrorContainer),
        background = colorResource(R.color.md_theme_light_background),
        onBackground = colorResource(R.color.md_theme_light_onBackground),
        surface = colorResource(R.color.md_theme_light_surface),
        onSurface = colorResource(R.color.md_theme_light_onSurface),
        surfaceVariant = colorResource(R.color.md_theme_light_surfaceVariant),
        onSurfaceVariant = colorResource(R.color.md_theme_light_onSurfaceVariant),
        outline = colorResource(R.color.md_theme_light_outline),
        inverseOnSurface = colorResource(R.color.md_theme_light_inverseOnSurface),
        inverseSurface = colorResource(R.color.md_theme_light_inverseSurface),
    )

private val DarkColorScheme
    @Composable
    get() = darkColorScheme(
        primary = colorResource(R.color.md_theme_dark_primary),
        onPrimary = colorResource(R.color.md_theme_dark_onPrimary),
        primaryContainer = colorResource(R.color.md_theme_dark_primaryContainer),
        onPrimaryContainer = colorResource(R.color.md_theme_dark_onPrimaryContainer),
        secondary = colorResource(R.color.md_theme_dark_secondary),
        onSecondary = colorResource(R.color.md_theme_dark_onSecondary),
        secondaryContainer = colorResource(R.color.md_theme_dark_secondaryContainer),
        onSecondaryContainer = colorResource(R.color.md_theme_dark_onSecondaryContainer),
        tertiary = colorResource(R.color.md_theme_dark_tertiary),
        onTertiary = colorResource(R.color.md_theme_dark_onTertiary),
        tertiaryContainer = colorResource(R.color.md_theme_dark_tertiaryContainer),
        onTertiaryContainer = colorResource(R.color.md_theme_dark_onTertiaryContainer),
        error = colorResource(R.color.md_theme_dark_error),
        errorContainer = colorResource(R.color.md_theme_dark_errorContainer),
        onError = colorResource(R.color.md_theme_dark_onError),
        onErrorContainer = colorResource(R.color.md_theme_dark_onErrorContainer),
        background = colorResource(R.color.md_theme_dark_background),
        onBackground = colorResource(R.color.md_theme_dark_onBackground),
        surface = colorResource(R.color.md_theme_dark_surface),
        onSurface = colorResource(R.color.md_theme_dark_onSurface),
        surfaceVariant = colorResource(R.color.md_theme_dark_surfaceVariant),
        onSurfaceVariant = colorResource(R.color.md_theme_dark_onSurfaceVariant),
        outline = colorResource(R.color.md_theme_dark_outline),
        inverseOnSurface = colorResource(R.color.md_theme_dark_inverseOnSurface),
        inverseSurface = colorResource(R.color.md_theme_dark_inverseSurface),
    )

@SuppressLint("NewApi")
@Composable
fun FotomatorTheme(
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // TODO: For now, use both Material 2 and Material 3 themes, since not all components exist in Material 3
    androidx.compose.material.MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}