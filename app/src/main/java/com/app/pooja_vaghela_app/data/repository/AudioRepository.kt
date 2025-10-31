package com.app.pooja_vaghela_app.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.app.pooja_vaghela_app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRepository(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentRecordingUri: Uri? = null

    fun playAudio() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.dummy_audio)
                mediaPlayer?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer?.isLooping = true
            }

            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true

            mediaPlayer?.start()
            Log.d("AudioRepository", "Sample audio is playing on speaker.")

        } catch (e: Exception) {
            Log.e("AudioRepository", "Error playing audio: ${e.message}")
        }
    }

    fun pauseAudio() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.e("AudioRepository", "error in pause: ${e.message}")
        }
    }

    fun releasePlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioRepository", "error in release: ${e.message}")
        }
    }

    fun switchToEarpiece() {
        try {
            if (mediaPlayer == null) return

            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val earpieceDevice = audioManager.availableCommunicationDevices
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                earpieceDevice?.let {
                    audioManager.setCommunicationDevice(it)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Failed - switch to earpiece: ${e.message}")
        }
    }

    fun switchToSpeaker() {
        try {
            if (mediaPlayer == null) return

            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val speakerDevice = audioManager.availableCommunicationDevices
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                speakerDevice?.let {
                    audioManager.setCommunicationDevice(it)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Failed - switch to speaker: ${e.message}")
        }
    }

    private var audioFocusListener: AudioManager.OnAudioFocusChangeListener? = null

    fun startRecording() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "record_$timeStamp.3gp"

            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/PoojaVaghelaApp")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(audioCollection, values)
            currentRecordingUri = uri

            val fileDescriptor = uri?.let { resolver.openFileDescriptor(it, "w") } ?: return

            audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS -> Log.d("AudioRepository", "music paused from system")

                    AudioManager.AUDIOFOCUS_GAIN -> Log.d("AudioRepository", "music can resume")
                }
            }

            val result = audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(fileDescriptor.fileDescriptor)
                    prepare()
                    start()
                }
            }

        } catch (e: Exception) {
            Log.e("AudioRepository", "error recording : ${e.message}")
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            currentRecordingUri?.let { uri ->
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, values, null, null)
            }

            audioManager.abandonAudioFocus(audioFocusListener)
            Log.d("AudioRepository", "Recording stopped and saved to Music/Pooja Vaghela App")

        } catch (e: Exception) {
            Log.e("AudioRepository", "Stop recording error: ${e.message}")
        }
    }

}
