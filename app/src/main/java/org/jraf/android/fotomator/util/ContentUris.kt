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