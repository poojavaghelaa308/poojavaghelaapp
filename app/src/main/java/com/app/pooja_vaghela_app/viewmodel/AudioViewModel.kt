package com.app.pooja_vaghela_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.pooja_vaghela_app.data.repository.AudioRepository

class AudioViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AudioRepository(app)

    fun playAudio() = repository.playAudio()
    fun pauseAudio() = repository.pauseAudio()
    fun releasePlayer() = repository.releasePlayer()
    fun switchToEarpiece() = repository.switchToEarpiece()
    fun switchToSpeaker() = repository.switchToSpeaker()
    fun startRecording() = repository.startRecording()
    fun stopRecording() = repository.stopRecording()
    fun stopAudio() = repository.stopAudio()  // add stop method
    fun isPlaying() = repository.isPlaying()
}