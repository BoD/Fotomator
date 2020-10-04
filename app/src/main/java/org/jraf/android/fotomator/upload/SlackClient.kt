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
            authorization = "Bearer $TOKEN_XXX_PRIVATE_DO_NOT_PUBLISH",
            channels = channels,
            file = part
        )
        return fileUploadResponse.ok
    }

    companion object {
        private const val SLACK_BASE_URI = "https://slack.com/api/"

        private const val TOKEN_XXX_PRIVATE_DO_NOT_PUBLISH = "xoxp-"
    }
}