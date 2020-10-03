package org.jraf.android.fotomator.app.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.jraf.android.kprefs.Prefs

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = Prefs(application)

    val isServiceEnabled: MutableLiveData<Boolean> by prefs.BooleanLiveData(false)
}