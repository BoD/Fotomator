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
package org.jraf.android.fotomator.app.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.BuildConfig
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.slack.auth.SlackAuthActivity
import org.jraf.android.fotomator.app.slack.channel.SlackPickChannelActivity
import org.jraf.android.fotomator.monitoring.PhotoMonitoringService
import org.jraf.android.fotomator.util.observeNonNull
import org.jraf.android.util.about.AboutActivityIntentBuilder
import org.jraf.android.util.dialog.AlertDialogFragment
import org.jraf.android.util.dialog.AlertDialogListener
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AlertDialogListener {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isServiceEnabled by viewModel.isServiceEnabledLiveData.observeAsState(false)
            val slackChannel by viewModel.slackChannelLiveData.observeAsState()
            val slackTeamName by viewModel.slackTeamName.observeAsState()
            val automaticallyStopServiceDateTimeFormatted by viewModel.automaticallyStopServiceDateTimeFormatted.observeAsState("")
            val isAutomaticallyStopServiceDialogVisible by viewModel.isAutomaticallyStopServiceDialogVisible.observeAsState(false)
            MainLayout(
                isServiceEnabled = isServiceEnabled,
                slackChannel = slackChannel,
                slackTeamName = slackTeamName,
                automaticallyStopServiceDateTimeFormatted = automaticallyStopServiceDateTimeFormatted,
                onServiceEnabledClick = viewModel::onServiceEnabledSwitchClick,
                onAboutClick = ::onAboutClick,
                onDisconnectSlackClick = viewModel::onDisconnectSlackClick,
                onChannelClick = viewModel::onChannelClick,
                onAutomaticallyStopServiceDateTimeClick = viewModel::onAutomaticallyStopServiceDateTimeClick,
                isAutomaticallyStopServiceDialogVisible = isAutomaticallyStopServiceDialogVisible,
                onAutomaticallyStopServiceDialogSetDateTimeClick = viewModel::onAutomaticallyStopServiceDialogSetDateTimeClick,
                onAutomaticallyStopServiceDialogManuallyClick = viewModel::onAutomaticallyStopServiceDialogManuallyClick,
            )
        }

        observeViewModel()

        checkPermissions()

        viewModel.handleConfigureIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) viewModel.handleConfigureIntent(intent)
    }

    private fun observeViewModel() {
        viewModel.isServiceEnabledLiveData.observe(this) { serviceEnabled ->
            Log.d("serviceEnabled=$serviceEnabled")
            if (serviceEnabled) {
                startPhotoMonitoringService()
            } else {
                stopPhotoMonitoringService()
            }
        }

        viewModel.pickSlackChannel.observeNonNull(this) {
            setupSlackChannel()
        }

        viewModel.showAutomaticallyStopServiceDatePicker.observeNonNull(this) {
            showAutomaticallyStopServiceDatePicker()
        }

        viewModel.showAutomaticallyStopServiceTimePicker.observeNonNull(this) {
            showAutomaticallyStopServiceTimePicker()
        }

        viewModel.automaticallyStopServiceDateIsInThePast.observeNonNull(this) {
            Toast.makeText(this, R.string.main_automaticallyStopServiceDialog_dateIsInThePast, Toast.LENGTH_LONG).show()
        }

        viewModel.setupSlackAuth.observeNonNull(this) {
            setupSlackAuth()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        Log.d("isGranted=$isGranted")
        if (isGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsNotGranted()
        }
    }

    private val setupSlackAuthLauncher = registerForActivityResult(SlackAuthActivity.CONTRACT) { result ->
        Log.d("result=$result")
        if (!result) {
            if (viewModel.slackAuthToken == null) {
                Log.d("User refuses to setup Slack auth: finish")
                finish()
            }
        } else {
            if (viewModel.slackChannel == null) setupSlackChannel()
        }
    }

    private val pickSlackChannelLauncher = registerForActivityResult(SlackPickChannelActivity.CONTRACT) { pickedChannel ->
        Log.d("pickedChannel=$pickedChannel")
        if (pickedChannel == null) {
            if (viewModel.slackChannel == null) {
                Log.d("User didn't pick a Slack channel: finish")
                finish()
            }
        } else {
            viewModel.slackChannel = pickedChannel
        }
    }

    private fun checkPermissions() {
        Log.d()
        if (ContextCompat.checkSelfPermission(this, PERMISSION_TO_REQUEST) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(PERMISSION_TO_REQUEST)) {
                showPermissionRationaleUi()
            } else {
                requestPermission()
            }
        } else {
            onPermissionsGranted()
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(PERMISSION_TO_REQUEST)
    }

    private fun onPermissionsNotGranted() {
        val shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale(PERMISSION_TO_REQUEST)
        Log.d("shouldShowRequestPermissionRationale=$shouldShowRequestPermissionRationale")
        if (shouldShowRequestPermissionRationale) {
            checkPermissions()
        } else {
            AlertDialogFragment.newInstance(DIALOG_NO_PERMISSION)
                .message(R.string.main_permissionNotGrantedDialog_message)
                .positiveButton(R.string.main_permissionNotGrantedDialog_positive)
                .negativeButton(R.string.main_permissionNotGrantedDialog_negative)
                .show(this)
        }
    }

    private fun showPermissionRationaleUi() {
        Log.d()
        AlertDialogFragment.newInstance(DIALOG_SHOW_RATIONALE)
            .message(R.string.main_permissionShowRationaleDialog_message)
            .positiveButton(R.string.main_permissionShowRationaleDialog_positive)
            .show(this)
    }

    private fun onPermissionsGranted() {
        Log.d()
        when {
            viewModel.slackAuthToken == null -> setupSlackAuth()
            viewModel.slackChannel == null -> setupSlackChannel()
        }
    }

    private fun setupSlackAuth() {
        setupSlackAuthLauncher.launch()
    }

    private fun setupSlackChannel() {
        pickSlackChannelLauncher.launch()
    }


    private fun startPhotoMonitoringService() {
        if (!PhotoMonitoringService.isStarted) {
            Toast.makeText(this, R.string.main_service_toast_enabled, Toast.LENGTH_LONG).show()
            val serviceIntent = Intent(this, PhotoMonitoringService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    private fun stopPhotoMonitoringService() {
        if (PhotoMonitoringService.isStarted) {
            Toast.makeText(this, R.string.main_service_toast_disabled, Toast.LENGTH_LONG).show()
            stopService(Intent(this, PhotoMonitoringService::class.java))
        }
    }

    override fun onDialogClickPositive(tag: Int, payload: Any?) {
        when (tag) {
            DIALOG_SHOW_RATIONALE -> requestPermission()
            DIALOG_NO_PERMISSION -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDialogClickNegative(tag: Int, payload: Any?) {
        when (tag) {
            DIALOG_NO_PERMISSION -> finish()
        }
    }

    override fun onDialogClickListItem(tag: Int, index: Int, payload: Any?) {}

    private fun onAboutClick() {
        startActivity(
            AboutActivityIntentBuilder()
                .setAppName(getString(R.string.app_name))
                .setBuildDate(BuildConfig.BUILD_DATE)
                .setGitSha1(BuildConfig.GIT_SHA1)
                .setAuthorCopyright(getString(R.string.about_authorCopyright))
                .setLicense(getString(R.string.about_License))
                .setShareTextSubject(getString(R.string.about_shareText_subject))
                .setShareTextBody(getString(R.string.about_shareText_body))
                .setBackgroundResId(R.drawable.about_bg)
                .setShowOpenSourceLicencesLink(true)
                .addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text))
                .addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text))
                .addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text))
                .setIsLightIcons(true)
                .build(this)
        )
    }

    //--------------------------------------------------------------------------
    // region Automatically stop service date/time.
    //--------------------------------------------------------------------------

    private fun showAutomaticallyStopServiceDatePicker() {
        if (supportFragmentManager.findFragmentByTag(DIALOG_AUTOMATICALLY_STOP_SERVICE_DATE) != null) return
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.main_automaticallyStopServiceDialog_title)
            .setCalendarConstraints(CalendarConstraints.Builder().setStart(System.currentTimeMillis()).build())
            .build()
        datePicker.addOnNegativeButtonClickListener {
            Log.d()
            viewModel.onAutomaticallyStopServiceDatePicked(null)
        }
        datePicker.addOnCancelListener {
            Log.d()
            viewModel.onAutomaticallyStopServiceDatePicked(null)
        }
        datePicker.addOnPositiveButtonClickListener { timestamp ->
            Log.d("timestamp=$timestamp}")
            viewModel.onAutomaticallyStopServiceDatePicked(timestamp)
        }
        datePicker.show(supportFragmentManager, DIALOG_AUTOMATICALLY_STOP_SERVICE_DATE)
    }

    private fun showAutomaticallyStopServiceTimePicker() {
        if (supportFragmentManager.findFragmentByTag(DIALOG_AUTOMATICALLY_STOP_SERVICE_TIME) != null) return
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(R.string.main_automaticallyStopServiceDialog_title)
            .setTimeFormat(if (DateFormat.is24HourFormat(this)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .build()
        timePicker.addOnNegativeButtonClickListener {
            Log.d()
            viewModel.onAutomaticallyStopServiceTimePicked(null, null)
        }
        timePicker.addOnCancelListener {
            Log.d()
            viewModel.onAutomaticallyStopServiceTimePicked(null, null)
        }
        timePicker.addOnPositiveButtonClickListener {
            Log.d()
            viewModel.onAutomaticallyStopServiceTimePicked(timePicker.hour, timePicker.minute)
        }
        timePicker.show(supportFragmentManager, DIALOG_AUTOMATICALLY_STOP_SERVICE_TIME)
    }

    // endregion

    companion object {
        private const val PERMISSION_TO_REQUEST = Manifest.permission.READ_EXTERNAL_STORAGE

        private const val DIALOG_NO_PERMISSION = 0
        private const val DIALOG_SHOW_RATIONALE = 1
        private const val DIALOG_AUTOMATICALLY_STOP_SERVICE_DATE = "DIALOG_AUTOMATICALLY_STOP_SERVICE_DATE"
        private const val DIALOG_AUTOMATICALLY_STOP_SERVICE_TIME = "DIALOG_AUTOMATICALLY_STOP_SERVICE_TIME"
    }
}
