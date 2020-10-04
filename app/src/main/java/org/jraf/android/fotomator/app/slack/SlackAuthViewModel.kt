package org.jraf.android.fotomator.app.slack

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.prefs.AppPrefs
import org.jraf.android.fotomator.upload.SlackClient
import org.jraf.android.util.log.Log

class SlackAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val slackClient = SlackClient()
    private val prefs = AppPrefs(application)

    val isLoadingVisible = MutableLiveData(false)
    val isStartSlackAuthButtonVisible = MutableLiveData(true)
    val toast = MutableLiveData<Int>()
    val finish = MutableLiveData<Unit>()

    fun checkForToken() {
        // If we already have a token, finish immediately.
        // This avoids the case where a user keeps an old slack auth webpage open which will redirect here for no good reason
        if (prefs.slackAuthToken != null) finish.value = Unit
    }

    fun startAuthentication() {
        Log.d()
        setLoading(true)
        prefs.slackAuthToken = null
        getApplication<Application>().startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(SlackClient.SLACK_APP_AUTHORIZE_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
                        finish.value = Unit
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoadingVisible.value = loading
        isStartSlackAuthButtonVisible.value = !loading
    }

    private fun showToast(@StringRes resId: Int) {
        toast.value = resId
        toast.value = null
    }

    companion object {
        private const val QUERY_PARAM_ERROR = "error"
        private const val QUERY_PARAM_CODE = "code"
    }
}
