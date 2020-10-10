/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2020-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.fotomator.util

import android.net.Uri

/**
 * Removes any ID from the end of the path.
 *
 * @return a new URI with the ID removed from the end of the path, or the
 * original URI when it has no ID to remove from the end of the path
 */
fun Uri.withoutId(): Uri {
    // Verify that we have a valid ID to actually remove
    val last = lastPathSegment
    last?.toLong() ?: return this

    val segments = pathSegments
    val builder = buildUpon()
    builder.path(null)
    for (i in 0 until segments.size - 1) {
        builder.appendPath(segments[i])
    }
    return builder.build()
}