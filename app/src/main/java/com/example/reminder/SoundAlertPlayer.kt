package com.example.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SoundAlertPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val lock = Any()
    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    private var shouldBePlaying = false

    suspend fun startAlarmLoop() = withContext(Dispatchers.IO) {
        synchronized(lock) {
            shouldBePlaying = true
            releasePlayerLocked()

            val alarmUri = resolveAlarmUri() ?: return@withContext
            val player = MediaPlayer()
            val started = runCatching {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                player.setDataSource(appContext, alarmUri)
                player.isLooping = true
                player.prepare()
                if (shouldBePlaying) {
                    player.start()
                    mediaPlayer = player
                    true
                } else {
                    player.release()
                    false
                }
            }.getOrElse {
                runCatching { player.release() }
                false
            }

            if (!started) {
                mediaPlayer = null
            }
        }
    }

    fun stopAlarm() {
        synchronized(lock) {
            shouldBePlaying = false
            releasePlayerLocked()
        }
    }

    private fun releasePlayerLocked() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching {
                player.reset()
                player.release()
            }
        }
        mediaPlayer = null
    }

    private fun resolveAlarmUri(): Uri? {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
}
