package org.jraf.android.fotomator.upload.retrofit.apimodels.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val ok: Boolean
)
