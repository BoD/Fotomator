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
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.slack.auth.SlackAuthActivity
import org.jraf.android.fotomator.app.slack.channel.SlackPickChannelActivity
import org.jraf.android.fotomator.databinding.MainActivityBinding
import org.jraf.android.fotomator.monitoring.PhotoMonitoringService
import org.jraf.android.util.dialog.AlertDialogFragment
import org.jraf.android.util.dialog.AlertDialogListener
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AlertDialogListener {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        observeUi()

        checkPermissions()
    }

    private fun observeUi() {
        viewModel.isServiceEnabled.observe(this) { serviceEnabled ->
            Log.d("serviceEnabled=$serviceEnabled")
            if (serviceEnabled) {
                startPhotoMonitoringService()
            } else {
                stopPhotoMonitoringService()
            }
        }

        viewModel.pickSlackChannel.observe(this) { pickSlackChannel ->
            if (pickSlackChannel != null) setupSlackChannel()
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

    private val setupSlackAuthLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        Log.d("result=$result")
        if (result.resultCode == Activity.RESULT_OK) {
            if (viewModel.slackChannel == null) setupSlackChannel()
        }
    }

    private val pickSlackChannelLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        Log.d("result=$result")
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedChannel = SlackPickChannelActivity.getPickedChannelName(result.data!!)
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
        setupSlackAuthLauncher.launch(Intent(this, SlackAuthActivity::class.java))
    }

    private fun setupSlackChannel() {
        pickSlackChannelLauncher.launch(Intent(this, SlackPickChannelActivity::class.java))
    }


    private fun startPhotoMonitoringService() {
        if (!PhotoMonitoringService.isStarted) {
            Toast.makeText(this, R.string.main_service_toast_enabled, Toast.LENGTH_LONG).show()
            val serviceIntent = Intent(this, PhotoMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
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

    companion object {
        private const val PERMISSION_TO_REQUEST = Manifest.permission.READ_EXTERNAL_STORAGE

        private const val DIALOG_NO_PERMISSION = 0
        private const val DIALOG_SHOW_RATIONALE = 1
    }
}
