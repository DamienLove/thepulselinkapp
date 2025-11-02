package com.pulselink.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.pulselink.domain.model.Contact
import com.pulselink.domain.model.EscalationTier
import com.pulselink.domain.model.SoundOption
import com.pulselink.ui.state.PulseLinkUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PulseLinkUiState,
    onToggleListening: (Boolean) -> Unit,
    onSendCheckIn: () -> Unit,
    onTriggerTest: () -> Unit,
    onAddContact: (Contact) -> Unit,
    onDeleteContact: (Long) -> Unit,
    onSelectEmergencySound: (String) -> Unit,
    onSelectCheckInSound: (String) -> Unit
) {
    val showAddDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "PulseLink") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog.value = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListeningCard(state, onToggleListening)
            ActionsRow(onSendCheckIn, onTriggerTest, state)
            SoundSelectionCard(
                state = state,
                onSelectEmergencySound = onSelectEmergencySound,
                onSelectCheckInSound = onSelectCheckInSound
            )
            ContactsCard(state, onDeleteContact)
            AlertHistoryCard(state)
        }
    }

    if (showAddDialog.value) {
        AddContactDialog(
            onDismiss = { showAddDialog.value = false },
            onSave = { contact ->
                onAddContact(contact)
                showAddDialog.value = false
            }
        )
    }
}

@Composable
private fun ListeningCard(state: PulseLinkUiState, onToggleListening: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (state.isListening) state.settings.primaryPhrase else "Listening is paused",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (state.isListening) "Listening active" else "Tap to resume")
                Switch(checked = state.isListening, onCheckedChange = onToggleListening)
            }
            if (state.permissionHints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                state.permissionHints.forEach { hint ->
                    Text(text = "• $hint", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ActionsRow(
    onSendCheckIn: () -> Unit,
    onTriggerTest: () -> Unit,
    state: PulseLinkUiState
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Quick actions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSendCheckIn, enabled = !state.isDispatching) {
                    Text(text = "Check-in")
                }
                OutlinedButton(onClick = onTriggerTest, enabled = !state.isDispatching) {
                    Text(text = "Test Alert")
                }
            }
            state.lastMessagePreview?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Last trigger: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SoundSelectionCard(
    state: PulseLinkUiState,
    onSelectEmergencySound: (String) -> Unit,
    onSelectCheckInSound: (String) -> Unit
) {
    val emergencyOptions = state.emergencySoundOptions
    val checkInOptions = state.checkInSoundOptions
    if (emergencyOptions.isEmpty() && checkInOptions.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Alert sounds", style = MaterialTheme.typography.titleMedium)
            if (emergencyOptions.isNotEmpty()) {
                SoundPicker(
                    label = "Emergency alert sound",
                    options = emergencyOptions,
                    selectedKey = state.settings.emergencyProfile.soundKey,
                    onSelect = onSelectEmergencySound
                )
            }
            if (checkInOptions.isNotEmpty()) {
                SoundPicker(
                    label = "Check-in sound",
                    options = checkInOptions,
                    selectedKey = state.settings.checkInProfile.soundKey,
                    onSelect = onSelectCheckInSound
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPicker(
    label: String,
    options: List<SoundOption>,
    selectedKey: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.key == selectedKey } ?: options.firstOrNull()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = if (options.isNotEmpty()) !expanded else false }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedOption?.label ?: "Unavailable",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = options.isNotEmpty(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelect(option.key)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun ContactsCard(
    state: PulseLinkUiState,
    onDeleteContact: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Trusted contacts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (state.contacts.isEmpty()) {
                Text(text = "Add at least one contact to enable alerts.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.contacts, key = { it.id }) { contact ->
                        ContactRow(contact = contact, onDelete = { onDeleteContact(contact.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = contact.displayName, style = MaterialTheme.typography.bodyLarge)
                Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodySmall)
                Text(text = contact.escalationTier.name, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun AlertHistoryCard(state: PulseLinkUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Recent alerts", style = MaterialTheme.typography.titleMedium)
            if (state.recentEvents.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "No alerts yet.")
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                state.recentEvents.take(5).forEach { event ->
                    Text(
                        text = "${event.tier}: ${event.triggeredBy} → ${event.contactCount} contacts",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun AddContactDialog(onDismiss: () -> Unit, onSave: (Contact) -> Unit) {
    val nameState = remember { mutableStateOf(TextFieldValue()) }
    val phoneState = remember { mutableStateOf(TextFieldValue()) }
    val tierState = remember { mutableStateOf(EscalationTier.EMERGENCY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add trusted contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = phoneState.value,
                    onValueChange = { phoneState.value = it },
                    label = { Text("Phone") }
                )
                TierSelector(tierState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nameState.value.text.isNotBlank() && phoneState.value.text.isNotBlank()) {
                    onSave(
                        Contact(
                            displayName = nameState.value.text,
                            phoneNumber = phoneState.value.text,
                            escalationTier = tierState.value
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TierSelector(state: MutableState<EscalationTier>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Escalation tier", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { state.value = EscalationTier.EMERGENCY },
                enabled = state.value != EscalationTier.EMERGENCY
            ) { Text("Emergency") }
            OutlinedButton(
                onClick = { state.value = EscalationTier.CHECK_IN },
                enabled = state.value != EscalationTier.CHECK_IN
            ) { Text("Check-in") }
        }
    }
}
