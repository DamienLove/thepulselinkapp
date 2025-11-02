package com.pulselink.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pulselink.service.PulseLinkForegroundService

class TriggerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            PulseLinkForegroundService.enqueue(context)
        }
    }
}
