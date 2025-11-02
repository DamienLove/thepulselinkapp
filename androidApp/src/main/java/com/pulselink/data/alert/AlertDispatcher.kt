package com.pulselink.data.alert

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import com.pulselink.R
import com.pulselink.data.location.LocationProvider
import com.pulselink.data.sms.SmsSender
import com.pulselink.domain.model.AlertProfile
import com.pulselink.domain.model.Contact
import com.pulselink.domain.model.EscalationTier
import com.pulselink.domain.model.SoundCategory
import com.pulselink.domain.model.PulseLinkSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsSender: SmsSender,
    private val locationProvider: LocationProvider,
    private val registrar: NotificationRegistrar,
    private val soundCatalog: SoundCatalog
) {

    suspend fun dispatch(
        phrase: String,
        tier: EscalationTier,
        contacts: List<Contact>,
        settings: PulseLinkSettings
    ): AlertResult = withContext(Dispatchers.IO) {
        registrar.ensureChannels()

        val locationText = if (settings.includeLocation) buildLocationText() else null
        val message = buildMessage(phrase, tier, locationText)

        val (profile, notificationChannel) = when (tier) {
            EscalationTier.EMERGENCY -> settings.emergencyProfile to NotificationRegistrar.CHANNEL_ALERTS
            EscalationTier.CHECK_IN -> settings.checkInProfile to NotificationRegistrar.CHANNEL_CHECK_INS
        }
        val soundCategory = when (tier) {
            EscalationTier.EMERGENCY -> SoundCategory.SIREN
            EscalationTier.CHECK_IN -> SoundCategory.CHIME
        }

        val smsCount = runCatching {
            sendSms(message, contacts)
        }.getOrDefault(0)

        sendNotification(
            channel = notificationChannel,
            tier = tier,
            message = message,
            profile = profile,
            soundCategory = soundCategory,
            primaryContact = contacts.firstOrNull()
        )

        AlertResult(message = message, notifiedContacts = smsCount, sharedLocation = locationText != null)
    }

    private suspend fun buildLocationText(): String? {
        val location = runCatching { locationProvider.lastKnownLocation() }.getOrNull() ?: return null
        val geoUri = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        val timestamp = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date())
        return "Last known location @ $timestamp: $geoUri"
    }

    @RequiresPermission(Manifest.permission.SEND_SMS)
    private fun sendSms(message: String, contacts: List<Contact>): Int {
        return smsSender.sendAlert(message, contacts)
    }

    private fun buildMessage(phrase: String, tier: EscalationTier, locationText: String?): String {
        val header = when (tier) {
            EscalationTier.EMERGENCY -> "EMERGENCY" to "I need help right now."
            EscalationTier.CHECK_IN -> "CHECK-IN" to "I'm requesting a quick check-in."
        }
        val builder = StringBuilder()
            .append("PulseLink ${header.first}: ${header.second}\n")
            .append("Phrase triggered: \"")
            .append(phrase)
            .append("\".")
        if (locationText != null) {
            builder.append("\n").append(locationText)
        }
        builder.append("\nThis message was sent automatically via PulseLink.")
        return builder.toString()
    }

    private fun sendNotification(
        channel: String,
        tier: EscalationTier,
        message: String,
        profile: AlertProfile,
        soundCategory: SoundCategory,
        primaryContact: Contact?
    ) {
        val manager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(
                if (tier == EscalationTier.EMERGENCY) context.getString(R.string.notification_title_alert)
                else context.getString(R.string.notification_title_check_in)
            )
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        if (profile.vibrate) builder.setVibrate(longArrayOf(0, 250, 250, 250, 500, 250))
        if (profile.breakThroughDnd) builder.setCategory(NotificationCompat.CATEGORY_ALARM)

        val soundOption = soundCatalog.resolve(profile.soundKey, soundCategory)
        val soundUri = soundOption?.let {
            Uri.parse("android.resource://${context.packageName}/${it.resId}")
        }
        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        val callIntent = primaryContact?.let {
            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${'$'}{it.phoneNumber}"))
            PendingIntentCompat.getActivity(
                context,
                it.id.toInt(),
                dial,
                0,
                true
            )
        }
        if (callIntent != null) {
            builder.addAction(
                R.drawable.ic_logo,
                context.getString(R.string.notification_action_call),
                callIntent
            )
        }

        manager.notify(tier.ordinal + 100, builder.build())
    }

    data class AlertResult(
        val message: String,
        val notifiedContacts: Int,
        val sharedLocation: Boolean
    )
}
