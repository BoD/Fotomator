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
package org.jraf.android.fotomator.upload.retrofit

import okhttp3.MultipartBody
import org.jraf.android.fotomator.upload.retrofit.apimodels.response.FileUploadResponse
import org.jraf.android.fotomator.upload.retrofit.apimodels.response.OauthAccessResponse
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface SlackRetrofitService {
    @Multipart
    @POST("files.upload")
    suspend fun filesUpload(
        @Header("Authorization")
        authorization: String,
        @Query("channels")
        channels: String,
        @Part
        file: MultipartBody.Part,
    ): FileUploadResponse

    @POST("oauth.v2.access")
    suspend fun oauthAccess(
        @Query("code")
        code: String,
        @Query("client_id")
        clientId: String,
        @Query("client_secret")
        clientSecret: String,
    ): OauthAccessResponse
}