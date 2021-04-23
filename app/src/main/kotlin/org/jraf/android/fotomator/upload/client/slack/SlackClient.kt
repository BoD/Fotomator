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
package org.jraf.android.fotomator.upload.client.slack

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jraf.android.fotomator.upload.client.slack.retrofit.SlackRetrofitService
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiChannel
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiConversationsListResponse
import org.jraf.android.util.log.Log
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream

class SlackClient(private val authTokenProvider: AuthTokenProvider) {
    private fun createRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(SLACK_BASE_URI)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service: SlackRetrofitService = createRetrofit().create(SlackRetrofitService::class.java)

    suspend fun oauthAccess(code: String): String? {
        try {
            val oauthAccessResponse = service.oauthAccess(
                code = code,
                clientId = SLACK_APP_CLIENT_ID,
                clientSecret = SLACK_APP_CLIENT_SECRET
            )
            if (!oauthAccessResponse.ok) return null
            return oauthAccessResponse.authedUser?.accessToken
        } catch (e: Exception) {
            Log.w(e, "Could not make network call")
            return null
        }
    }

    suspend fun uploadFile(fileInputStream: InputStream, channels: String): Boolean {
        val part = MultipartBody.Part.createFormData(
            "file",
            FILE_NAME,
            RequestBody.create(
                MediaType.parse("image/*"),
                fileInputStream.readBytes()
            )
        )

        return try {
            val fileUploadResponse = service.filesUpload(
                authorization = getAuthorizationHeader(),
                channels = channels,
                file = part
            )
            fileUploadResponse.ok
        } catch (e: Exception) {
            Log.w(e, "Could not make network call")
            false
        }
    }

    suspend fun getChannelList(): List<SlackChannel>? {
        val res = mutableListOf<SlackChannel>()
        var slackApiConversationsListResponse: SlackApiConversationsListResponse? = null
        return try {
            do {
                slackApiConversationsListResponse = service.conversationsList(
                    authorization = getAuthorizationHeader(),
                    cursor = slackApiConversationsListResponse?.responseMetadata?.nextCursor?.ifEmpty { null }
                )
                res += slackApiConversationsListResponse.channels.map { it.toBusiness() }
            } while (!slackApiConversationsListResponse?.responseMetadata?.nextCursor.isNullOrEmpty())
            res.sortedBy { it.name }
        } catch (e: Exception) {
            Log.w(e, "Could not make network call")
            null
        }
    }

    private fun getAuthorizationHeader() = "Bearer ${authTokenProvider.getAuthToken()}"

    companion object {
        private const val SLACK_BASE_URI = "https://slack.com/api/"
        private const val SLACK_APP_CLIENT_ID = "60118040739.1405861361203"
        private const val SLACK_APP_CLIENT_SECRET = "23e75fe20620320e2236325c41437420"

        const val SLACK_APP_AUTHORIZE_URL =
            "https://slack.com/oauth/v2/authorize?client_id=$SLACK_APP_CLIENT_ID&scope=&user_scope=files:write,channels:read,groups:read"

        private const val FILE_NAME = "via Fotomator"
    }
}

private fun SlackApiChannel.toBusiness() = SlackChannel(
    name = name,
    topic = topic?.value?.ifEmpty { null },
    purpose = purpose?.value?.ifEmpty { null },
)
