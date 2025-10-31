package com.app.pooja_vaghela_app.data.repository

import android.content.Context
import com.app.pooja_vaghela_app.model.NightModeSchedule

class SettingRepository (context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun saveNightModeSchedule(schedule: NightModeSchedule) {
        prefs.edit()
            .putInt("startHour", schedule.startHour)
            .putInt("startMinute", schedule.startMinute)
            .putInt("endHour", schedule.endHour)
            .putInt("endMinute", schedule.endMinute)
            .apply()
    }

    fun getNightModeSchedule(): NightModeSchedule {
        val startHour = prefs.getInt("startHour", 18)
        val startMinute = prefs.getInt("startMinute", 0)
        val endHour = prefs.getInt("endHour", 8)
        val endMinute = prefs.getInt("endMinute", 0)
        return NightModeSchedule(startHour, startMinute, endHour, endMinute)
    }
}