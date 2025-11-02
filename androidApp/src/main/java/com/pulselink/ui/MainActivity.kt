package com.pulselink.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulselink.ui.screens.HomeScreen
import com.pulselink.ui.state.MainViewModel
import com.pulselink.ui.theme.PulseLinkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseLinkTheme {
                val context = LocalContext.current
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.ensureServiceRunning(context)
                }

                LaunchedEffect(Unit) {
                    val permissions = buildList {
                        add(Manifest.permission.RECORD_AUDIO)
                        add(Manifest.permission.SEND_SMS)
                        add(Manifest.permission.RECEIVE_SMS)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                    viewModel.ensureServiceRunning(context)
                }

                HomeScreen(
                    state = state,
                    onToggleListening = viewModel::toggleListening,
                    onSendCheckIn = viewModel::sendCheckIn,
                    onTriggerTest = viewModel::triggerTest,
                    onAddContact = viewModel::saveContact,
                    onDeleteContact = viewModel::deleteContact,
                    onSelectEmergencySound = viewModel::updateEmergencySound,
                    onSelectCheckInSound = viewModel::updateCheckInSound
                )
            }
        }
    }
}
