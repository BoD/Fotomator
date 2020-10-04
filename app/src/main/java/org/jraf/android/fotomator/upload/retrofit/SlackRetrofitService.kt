package org.jraf.android.fotomator.upload.retrofit

import okhttp3.MultipartBody
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
        file: MultipartBody.Part
    ): FileUploadResponse
}