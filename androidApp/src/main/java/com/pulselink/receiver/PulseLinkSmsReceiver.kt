package com.pulselink.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pulselink.service.AlertRouter

@AndroidEntryPoint
class PulseLinkSmsReceiver : BroadcastReceiver() {

    @Inject lateinit var alertRouter: AlertRouter

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(separator = " ") { it.messageBody }
        if (body.isBlank()) return
        val extras: Bundle? = intent.extras
        CoroutineScope(Dispatchers.Default).launch {
            alertRouter.onInboundMessage(body)
        }
    }
}
