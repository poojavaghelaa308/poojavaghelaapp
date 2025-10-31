package com.app.pooja_vaghela_app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.app.pooja_vaghela_app.R
import com.app.pooja_vaghela_app.databinding.ActivityMainBinding
import com.app.pooja_vaghela_app.ui.setting.SettingActivity
import com.app.pooja_vaghela_app.utils.SensorUtils
import com.app.pooja_vaghela_app.viewmodel.AudioViewModel
import com.app.pooja_vaghela_app.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AudioViewModel by viewModels()
    private lateinit var sensorUtils: SensorUtils
    private var isRecording = false
    private val themeViewModel: ThemeViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
            val hasMediaPermission =
                permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                        permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

            if (hasAudioPermission && hasMediaPermission) {
                startAudioRecording()
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        themeViewModel.applySavedTheme()
        setContentView(binding.root)

        lifecycleScope.launch {
            while (true) {
                delay(60_000)
                themeViewModel.applySavedTheme()
            }
        }

        sensorUtils = SensorUtils(
            this,
            onNearEar = { viewModel.switchToEarpiece() },
            onAwayFromEar = { viewModel.switchToSpeaker() }
        )

        binding.btnPlay.setOnClickListener { viewModel.playAudio() }

        binding.btnRecord.setOnClickListener {
            if (!isRecording) {
                checkPermissionsAndRecord()
            } else {
                stopAudioRecording()
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun startAudioRecording() {
        try {
            viewModel.startRecording()
            binding.btnRecord.text = getString(R.string.stop_recording)
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.failed_to_start_recording, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun stopAudioRecording() {
        viewModel.stopRecording()
        binding.btnRecord.text = getString(R.string.start_recording)
        isRecording = false
        Toast.makeText(this, getString(R.string.recording_stopped), Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionsAndRecord() {
        val hasAudioPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasMediaPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        if (hasAudioPermission && hasMediaPermission) {
            startAudioRecording()
        } else {
            val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        sensorUtils.register()
        themeViewModel.applySavedTheme()
    }

    override fun onPause() {
        super.onPause()
        sensorUtils.unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }
}
