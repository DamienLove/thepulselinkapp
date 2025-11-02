package com.pulselink.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertProfile(
    @SerialName("sound")
    val soundKey: String? = null,
    val breakThroughDnd: Boolean = true,
    val vibrate: Boolean = true
)

@Serializable
data class PulseLinkSettings(
    val primaryPhrase: String = "help me pulselink",
    val secondaryPhrase: String = "check in pulselink",
    val listeningEnabled: Boolean = true,
    val includeLocation: Boolean = true,
    val emergencyProfile: AlertProfile = AlertProfile(),
    val checkInProfile: AlertProfile = AlertProfile(
        breakThroughDnd = false,
        vibrate = true
    ),
    val autoCallAfterAlert: Boolean = false
) {
    fun phrases(): List<String> = listOf(primaryPhrase, secondaryPhrase)
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
}
