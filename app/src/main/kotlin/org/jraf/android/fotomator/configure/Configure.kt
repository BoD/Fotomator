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
package org.jraf.android.fotomator.configure

import android.content.Intent
import org.jraf.android.fotomator.util.parseIsoDateHourMinute

private const val SCHEME = "fotomator"
private const val HOST = "configure"

/**
 * Configure link looks like:
 * fotomator://configure?channel=test3&end=2021-07-31T22:00Z
 */
data class Configure(
    val channel: String,
    val automaticallyStopServiceDateTime: Long?,
) {
    companion object {
        fun fromIntent(intent: Intent): Configure {
            val uri = intent.data!!
            val channel = uri.getQueryParameter("channel")!!.let {
                // Remove any # prefix (we don't want the #)
                if (it.startsWith('#')) {
                    it.substring(1)
                } else {
                    it
                }
            }
            val automaticallyStopServiceDateTime = uri.getQueryParameter("end")?.let { end ->
                runCatching { parseIsoDateHourMinute(end) }.getOrNull()
            }
            return Configure(
                channel = channel,
                automaticallyStopServiceDateTime = automaticallyStopServiceDateTime
            )
        }
    }
}

fun isConfigureIntent(intent: Intent): Boolean = intent.data?.let { uri ->
    uri.scheme == SCHEME && uri.host == HOST
} == true
