package org.jraf.android.fotomator.app.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import org.jraf.android.fotomator.R
import org.jraf.android.fotomator.app.slack.SlackAuthActivity
import org.jraf.android.fotomator.databinding.MainActivityBinding
import org.jraf.android.fotomator.monitoring.PhotoMonitoringService
import org.jraf.android.util.log.Log

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionsOk()
            } else {
                showNoPermissionUi()
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
        if (viewModel.slackAuthToken == null) {
            setupSlackAuth()
        } else {
            observeUi()
        }
    }

    private fun setupSlackAuth() {
        startActivity(Intent(this, SlackAuthActivity::class.java))
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
    }

    private fun startPhotoMonitoringService() {
        if (!PhotoMonitoringService.isMonitoring) {
            Toast.makeText(this, R.string.main_service_toast_enabled, Toast.LENGTH_LONG).show()
            startService(Intent(this, PhotoMonitoringService::class.java))
        }
    }

    private fun stopPhotoMonitoringService() {
        if (PhotoMonitoringService.isMonitoring) {
            Toast.makeText(this, R.string.main_service_toast_disabled, Toast.LENGTH_LONG).show()
            stopService(Intent(this, PhotoMonitoringService::class.java))
        }
    }
}
