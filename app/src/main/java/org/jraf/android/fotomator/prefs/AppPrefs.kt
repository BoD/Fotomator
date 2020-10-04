package org.jraf.android.fotomator.prefs

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.jraf.android.kprefs.Prefs

class AppPrefs(context: Context) {
    private val prefs = Prefs(context)

    val isServiceEnabled: MutableLiveData<Boolean> by prefs.BooleanLiveData(false)
    var slackAuthToken: String? by prefs.String()
}