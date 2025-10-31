package com.app.pooja_vaghela_app.ui.setting

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.app.pooja_vaghela_app.R
import com.app.pooja_vaghela_app.data.repository.SettingRepository
import com.app.pooja_vaghela_app.databinding.ActivitySettingBinding
import com.app.pooja_vaghela_app.model.NightModeSchedule
import com.app.pooja_vaghela_app.viewmodel.ThemeViewModel

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val viewModel: ThemeViewModel by viewModels()
    private lateinit var settingRepository: SettingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingRepository = SettingRepository(this)

        val schedule = settingRepository.getNightModeSchedule()
        binding.startPicker.setIs24HourView(true)
        binding.endPicker.setIs24HourView(true)
        binding.startPicker.hour = schedule.startHour
        binding.startPicker.minute = schedule.startMinute
        binding.endPicker.hour = schedule.endHour
        binding.endPicker.minute = schedule.endMinute

        binding.btnSave.setOnClickListener {
            val newSchedule = NightModeSchedule(
                binding.startPicker.hour,
                binding.startPicker.minute,
                binding.endPicker.hour,
                binding.endPicker.minute
            )

            viewModel.applyTheme(newSchedule)
            updateStatusBarAppearance()
            Toast.makeText(this, getString(R.string.schedule_saved_successfully), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateStatusBarAppearance() {
        val isDarkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme
    }
}
