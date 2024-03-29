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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jraf.android.fotomator.upload.client.slack.retrofit.SlackRetrofitService
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiChannel
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiConversationsListResponse
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiUser
import org.jraf.android.fotomator.upload.client.slack.retrofit.apimodels.response.SlackApiUsersInfoResponse
import org.jraf.android.util.log.Log
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream

class SlackClient(private val authTokenProvider: AuthTokenProvider) {
    private fun createRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(SLACK_BASE_URI)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        ).build())
        .build()

    private val service: SlackRetrofitService = createRetrofit().create(SlackRetrofitService::class.java)

    suspend fun oauthAccess(code: String): OAuthAccess {
        try {
            val oauthAccessResponse = service.oauthAccess(
                code = code,
                clientId = SLACK_APP_CLIENT_ID,
                clientSecret = SLACK_APP_CLIENT_SECRET
            )
            if (!oauthAccessResponse.ok) return OAuthAccess.Fail
            return OAuthAccess.Success(
                accessToken = oauthAccessResponse.authedUser!!.accessToken,
                teamName = oauthAccessResponse.team!!.name
            )
        } catch (e: Exception) {
            Log.w(e, "Could not make network call")
            return OAuthAccess.Fail
        }
    }

    suspend fun uploadFile(fileInputStream: InputStream, channelId: String): Boolean {
        val part = MultipartBody.Part.createFormData(
            "file",
            FILE_NAME,
            fileInputStream.readBytes().toRequestBody(contentType = "image/*".toMediaType())
        )

        return try {
            val fileUploadResponse = service.filesUpload(
                authorization = getAuthorizationHeader(),
                channelId = channelId,
                file = part
            )
            fileUploadResponse.ok
        } catch (e: Exception) {
            Log.w(e, "Could not make network call")
            false
        }
    }

    suspend fun getChannelList(): List<SlackChannelOrConversation>? {
        val res = mutableListOf<SlackChannelOrConversation>()
        var slackApiConversationsListResponse: SlackApiConversationsListResponse? = null
        val scope = CoroutineScope(Job())
        return try {
            do {
                slackApiConversationsListResponse = service.conversationsList(
                    authorization = getAuthorizationHeader(),
                    cursor = slackApiConversationsListResponse?.responseMetadata?.nextCursor?.ifEmpty { null }
                )
                val deferredSlackApiUsersInfoResponse = mutableListOf<Deferred<Pair<String, SlackApiUsersInfoResponse>>>()
                for (channel in slackApiConversationsListResponse.channels) {
                    when {
                        // Single conversation: retrieve the user's info asynchronously
                        channel.user != null -> {
                            if (!channel.isUserDeleted) {
                                deferredSlackApiUsersInfoResponse += scope.async {
                                    channel.id to service.usersInfo(
                                        authorization = getAuthorizationHeader(),
                                        user = channel.user
                                    )
                                }
                            }
                        }

                        // Group conversation
                        channel.isMpim -> res += channel.toGroupConversation()

                        // Channel
                        else -> res += channel.toSlackChannel()
                    }
                }

                // Add the single conversations obtained asynchronously earlier
                res += deferredSlackApiUsersInfoResponse.map {
                    val (id, slackApiUserResponse) = it.await()
                    slackApiUserResponse.user.toSingleConversation(id)
                }

            } while (!slackApiConversationsListResponse?.responseMetadata?.nextCursor.isNullOrEmpty())

            res.sortedBy { it.sortKey }
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

        private val SCOPES = arrayOf(
            "channels:read",
            "files:write",
            "groups:read",
            "im:read",
            "mpim:read",
            "users:read",
        )
            .joinToString(",")

        val SLACK_APP_AUTHORIZE_URL =
            "https://slack.com/oauth/v2/authorize?client_id=$SLACK_APP_CLIENT_ID&scope=&user_scope=$SCOPES"

        private const val FILE_NAME = "via Fotomator"
    }
}

private fun SlackApiChannel.toSlackChannel() = SlackChannel(
    id = id,
    name = name!!,
    topic = topic?.value?.ifEmpty { null },
    purpose = purpose?.value?.ifEmpty { null },
)

private fun SlackApiChannel.toGroupConversation() = SlackGroupConversation(
    id = id,
    description = purpose!!.value!!,
)

private fun SlackApiUser.toSingleConversation(id: String) = SlackSingleConversation(
    id = id,
    description = realName,
)

private val SlackChannelOrConversation.sortKey: String
    get() = when (this) {
        is SlackChannel -> "0$name"
        is SlackGroupConversation -> "1$description"
        is SlackSingleConversation -> "2$description"
    }

sealed class OAuthAccess {
    object Fail : OAuthAccess()
    data class Success(val accessToken: String, val teamName: String) : OAuthAccess()
}