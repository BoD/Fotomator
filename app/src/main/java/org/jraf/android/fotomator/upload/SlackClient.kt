package org.jraf.android.fotomator.upload

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jraf.android.fotomator.upload.retrofit.SlackRetrofitService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream

class SlackClient {
    private fun createRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(SLACK_BASE_URI)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service: SlackRetrofitService = createRetrofit().create(SlackRetrofitService::class.java)

    suspend fun uploadFile(fileInputStream: InputStream, channels: String): Boolean {
        val part = MultipartBody.Part.createFormData(
            "file",
            "image",
            RequestBody.create(
                MediaType.parse("image/*"),
                fileInputStream.readBytes()
            )
        )

        val fileUploadResponse = service.filesUpload(
            authorization = "Bearer TODO",
            channels = channels,
            file = part
        )
        return fileUploadResponse.ok
    }

    suspend fun oauthAccess(code: String): String? {
        val oauthAccessResponse = service.oauthAccess(
            code = code,
            clientId = SLACK_APP_CLIENT_ID,
            clientSecret = SLACK_APP_CLIENT_SECRET
        )
        if (!oauthAccessResponse.ok) return null
        return oauthAccessResponse.authedUser?.accessToken
    }

    companion object {
        private const val SLACK_BASE_URI = "https://slack.com/api/"
        private const val SLACK_APP_CLIENT_ID = "60118040739.1405861361203"
        private const val SLACK_APP_CLIENT_SECRET = "23e75fe20620320e2236325c41437420"

        const val SLACK_APP_AUTHORIZE_URL = "https://slack.com/oauth/v2/authorize?client_id=$SLACK_APP_CLIENT_ID&scope=&user_scope=files:write"
    }
}