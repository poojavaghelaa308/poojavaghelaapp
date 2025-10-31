package com.app.pooja_vaghela_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import com.app.pooja_vaghela_app.data.repository.SettingRepository
import com.app.pooja_vaghela_app.model.NightModeSchedule
import java.util.Calendar

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingRepository(app)

    fun applyTheme(schedule: NightModeSchedule) {
        repo.saveNightModeSchedule(schedule)
        val isNight = isDarkModeActive(schedule)
        Log.d("ThemeViewModel", "Current mode: ${if (isNight) "Dark" else "Light"}")
        AppCompatDelegate.setDefaultNightMode(
            if (isNight) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun loadSchedule(): NightModeSchedule = repo.getNightModeSchedule()

    fun isDarkModeActive(schedule: NightModeSchedule): Boolean {
        val now = Calendar.getInstance()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.startHour)
            set(Calendar.MINUTE, schedule.startMinute)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.endHour)
            set(Calendar.MINUTE, schedule.endMinute)
        }

        return if (end.before(start)) {
            now.after(start) || now.before(end)
        } else {
            now.after(start) && now.before(end)
        }
    }

    fun applySavedTheme() {
        val schedule = loadSchedule()
        val isNight = isDarkModeActive(schedule)
        AppCompatDelegate.setDefaultNightMode(
            if (isNight) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
