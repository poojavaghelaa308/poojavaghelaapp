package com.app.pooja_vaghela_app.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorUtils(
    context: Context,
    private val onNearEar: () -> Unit,
    private val onAwayFromEar: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    fun register() {
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("SensorUtils", "Proximity sensor registered")
        } ?: Log.e("SensorUtils", "No proximity sensor found!")
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
        Log.d("SensorUtils", "Proximity sensor unregistered")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.firstOrNull() ?: return
        val maxRange = proximitySensor?.maximumRange ?: return

        if (distance < maxRange) {
            Log.d("SensorUtils", "Near ear detected")
            onNearEar()
        } else {
            Log.d("SensorUtils", "Away from ear detected")
            onAwayFromEar()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
