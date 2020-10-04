package org.jraf.android.fotomator.app.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.jraf.android.fotomator.prefs.AppPrefs

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPrefs(application)

    val isServiceEnabled: MutableLiveData<Boolean> = prefs.isServiceEnabled
    val slackAuthToken: String? get() = prefs.slackAuthToken
}