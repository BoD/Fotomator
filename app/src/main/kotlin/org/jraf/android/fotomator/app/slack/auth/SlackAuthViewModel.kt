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
package org.jraf.android.fotomator.app.slack.auth

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.upload.client.SlackClient
import org.jraf.android.fotomator.util.fireAndForget
import org.jraf.android.util.log.Log

class SlackAuthViewModel @ViewModelInject constructor(
    private val application: Application,
    private val prefs: AppPrefs,
    private val slackClient: SlackClient
) : ViewModel() {

    val isLoadingVisible = MutableLiveData(false)
    val isStartSlackAuthButtonVisible = MutableLiveData(true)
    val toast = MutableLiveData<Int?>()
    val finishWithSuccess = MutableLiveData<Unit>()

    fun startAuthentication() {
        Log.d()
        setLoading(true)
        prefs.slackAuthToken = null
        application.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(SlackClient.SLACK_APP_AUTHORIZE_URL))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun handleRedirectUri(redirectUri: Uri) {
        Log.d("redirectUri=$redirectUri")
        val error = redirectUri.getQueryParameter(QUERY_PARAM_ERROR)
        val code = redirectUri.getQueryParameter(QUERY_PARAM_CODE)
        when {
            error != null -> {
                Log.w("Authentication error'd")
                showToast(R.string.slack_auth_failed)
                setLoading(false)
            }

            code == null -> {
                Log.w("Code not present in redirect uri")
                showToast(R.string.slack_auth_failed)
                setLoading(false)
            }

            else -> {
                Log.d("Calling oauth access API")
                setLoading(true)
                GlobalScope.launch(Dispatchers.Main) {
                    val authToken = slackClient.oauthAccess(code)
                    if (authToken == null) {
                        Log.w("No auth token in response")
                        showToast(R.string.slack_auth_failed)
                        setLoading(false)
                    } else {
                        prefs.slackAuthToken = authToken
                        showToast(R.string.slack_auth_success)
                        finishWithSuccess.value = Unit
                    }
                }
            }
        }
    }

     fun setLoading(loading: Boolean) {
         isLoadingVisible.value = loading
         isStartSlackAuthButtonVisible.value = !loading
     }

    private fun showToast(@StringRes resId: Int) {
        toast.fireAndForget(resId)
    }

    companion object {
        private const val QUERY_PARAM_ERROR = "error"
        private const val QUERY_PARAM_CODE = "code"
    }
}
