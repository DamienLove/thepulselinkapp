package com.pulselink.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class RestoreRingerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audio = context.getSystemService(AudioManager::class.java) ?: return
        audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }
}
