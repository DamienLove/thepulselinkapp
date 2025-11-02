package com.pulselink.data.alert

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.pulselink.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRegistrar @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureChannels() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val group = NotificationChannelGroup(GROUP_ALERTS, context.getString(R.string.channel_alerts))
            manager.createNotificationChannelGroup(group)

            val alerts = NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                enableVibration(true)
                setGroup(GROUP_ALERTS)
            }
            val checkIns = NotificationChannel(
                CHANNEL_CHECK_INS,
                context.getString(R.string.channel_check_ins),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { setGroup(GROUP_ALERTS) }
            val listening = NotificationChannel(
                CHANNEL_LISTENING,
                context.getString(R.string.channel_listening),
                NotificationManager.IMPORTANCE_MIN
            ).apply { setGroup(GROUP_ALERTS) }
            val background = NotificationChannel(
                CHANNEL_BACKGROUND,
                context.getString(R.string.channel_background),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setGroup(GROUP_ALERTS) }
            manager.createNotificationChannels(listOf(alerts, checkIns, listening, background))
        }
    }

    companion object {
        const val CHANNEL_ALERTS = "pulse_alerts"
        const val CHANNEL_CHECK_INS = "pulse_checkins"
        const val CHANNEL_LISTENING = "pulse_listening"
        const val CHANNEL_BACKGROUND = "pulse_background"
        private const val GROUP_ALERTS = "pulse_group_alerts"
    }
}
