package org.jraf.android.fotomator.upload.retrofit.apimodels.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OauthAccessResponse(
    val ok: Boolean,

    @Json(name = "authed_user")
    val authedUser: AuthedUser?,
)

@JsonClass(generateAdapter = true)
data class AuthedUser(
    @Json(name = "access_token")
    val accessToken: String?,
)
