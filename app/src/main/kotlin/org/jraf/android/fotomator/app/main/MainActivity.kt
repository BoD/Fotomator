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
import android.os.Build
import android.os.Bundle
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
import org.jraf.android.util.log.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        observeUi()
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

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        Log.d("isGranted=$isGranted")
        if (isGranted) {
            onPermissionsOk()
        } else {
            showNoPermissionUi()
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
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionRationaleUi(permission)
            } else {
                requestPermissionLauncher.launch(permission)
            }
        } else {
            onPermissionsOk()
        }
    }

    private fun showPermissionRationaleUi(permission: String) {
        // TODO Show permission rationale UI
        Log.d()
        requestPermissionLauncher.launch(permission)
    }

    private fun showNoPermissionUi() {
        // TODO Show no permission UI
        Log.d()
    }

    private fun onPermissionsOk() {
        when {
            viewModel.slackAuthToken == null -> {
                setupSlackAuth()
            }
            viewModel.slackChannel == null -> {
                setupSlackChannel()
            }
        }
    }

    private fun setupSlackAuth() {
        startActivity(Intent(this, SlackAuthActivity::class.java))
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
}
