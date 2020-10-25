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
package org.jraf.android.fotomator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Media(
    @PrimaryKey val uri: String,
    val uploadState: MediaUploadState,
    val uploadFailedCount: Int = 0,
)

enum class MediaUploadState(val dbValue: Int) {
    SCHEDULED(MEDIA_UPLOAD_STATE_DB_SCHEDULED),
    UPLOADING(MEDIA_UPLOAD_STATE_DB_UPLOADING),
    UPLOADED(MEDIA_UPLOAD_STATE_DB_UPLOADED),
    OPT_OUT(MEDIA_UPLOAD_STATE_DB_OPT_OUT),
    ERROR(MEDIA_UPLOAD_STATE_DB_ERROR),
}

const val MEDIA_UPLOAD_STATE_DB_SCHEDULED = 0
const val MEDIA_UPLOAD_STATE_DB_UPLOADING = 1
const val MEDIA_UPLOAD_STATE_DB_UPLOADED = 2
const val MEDIA_UPLOAD_STATE_DB_OPT_OUT = 3
const val MEDIA_UPLOAD_STATE_DB_ERROR = 4