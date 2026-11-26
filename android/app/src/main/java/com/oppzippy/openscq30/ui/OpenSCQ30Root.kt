package com.oppzippy.openscq30.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.features.preferences.preferredBackTransitionDirection
import com.oppzippy.openscq30.features.preferences.prefersAmoledBlack
import com.oppzippy.openscq30.features.preferences.prefersDarkTheme
import com.oppzippy.openscq30.features.preferences.prefersDynamicColor
import com.oppzippy.openscq30.features.preferences.prefersPredictiveBack
import com.oppzippy.openscq30.features.preferences.prefersSlideTransitions
import com.oppzippy.openscq30.features.soundcoredevice.service.ConnectionStatus
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.ui.deviceselection.DeviceSelectionScreen
import com.oppzippy.openscq30.ui.devicesettings.DeviceSettingsScreen
import com.oppzippy.openscq30.ui.soundcore.components.ConnectedBatteryInfo
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.isRtlLayout
import com.oppzippy.openscq30.ui.theme.popExitSignForDirection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val SettingsRevealSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 480f,
    visibilityThreshold = 0.001f,
)

@Composable
fun OpenSCQ30Root(viewModel: OpenSCQ30RootViewModel = hiltViewModel()) {
    viewModel.toastHandler.Show()
    OpenSCQ30Theme(
        darkTheme = prefersDarkTheme(),
        dynamicColor = prefersDynamicColor(),
        amoledBlack = prefersAmoledBlack(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .clipToBounds(),
            ) {
                WhatsNewPrompt(viewModel)
                OpenSCQ30Content(viewModel)
            }
        }
    }
}

@Composable
private fun WhatsNewPrompt(viewModel: OpenSCQ30RootViewModel) {
    if (viewModel.version2BreakingChangesMessage.shouldShow.collectAsState(false).value) {
        WhatsNew(
            messageHtml = stringResource(R.string.version_2_breaking_changes),
            onClose = { viewModel.version2BreakingChangesMessage.setShown() },
        )
    }
}

@Composable
private fun OpenSCQ30Content(viewModel: OpenSCQ30RootViewModel) {
    val predictiveBackEnabled = prefersPredictiveBack()
    val slideTransitionsEnabled = prefersSlideTransitions()
    val backDirection = preferredBackTransitionDirection()
    val isRtl = isRtlLayout()
    val coroutineScope = rememberCoroutineScope()

    val connectionStatus by viewModel.connectionStatusFlow.collectAsState()
    val deviceSettings by viewModel.deviceSettingsManager.collectAsState()
    val isViewingDeviceSettings by viewModel.isViewingDeviceSettings.collectAsState()

    val connectingMacAddress = (connectionStatus as? ConnectionStatus.Connecting)?.macAddress
    val connectedStatus = connectionStatus as? ConnectionStatus.Connected
    val connectedMacAddress = connectedStatus?.macAddress
    val activeSettings = if (connectedStatus != null) deviceSettings else null
    val showDeviceSettings = isViewingDeviceSettings && activeSettings != null

    val settingsReveal = remember { Animatable(if (showDeviceSettings) 1f else 0f) }
    var backGestureEdge by remember { mutableStateOf<Int?>(null) }

    val latestShowDeviceSettings by rememberUpdatedState(showDeviceSettings)
    val latestConnectingMac by rememberUpdatedState(connectingMacAddress)
    val hasActiveSettingsState = rememberUpdatedState(activeSettings != null)

    val shouldComposeSettings by remember {
        derivedStateOf { showDeviceSettings || settingsReveal.value > 0.001f }
    }
    val isSelectionCovered by remember {
        derivedStateOf { hasActiveSettingsState.value && settingsReveal.value >= 0.999f }
    }

    LaunchedEffect(showDeviceSettings, activeSettings == null, slideTransitionsEnabled) {
        if (activeSettings == null) {
            settingsReveal.snapTo(0f)
        } else if (showDeviceSettings) {
            backGestureEdge = null
            if (slideTransitionsEnabled) {
                settingsReveal.animateTo(1f, SettingsRevealSpringSpec)
            } else {
                settingsReveal.snapTo(1f)
            }
        } else {
            if (slideTransitionsEnabled) {
                settingsReveal.animateTo(0f, SettingsRevealSpringSpec)
            } else {
                settingsReveal.snapTo(0f)
            }
        }
    }

    LaunchedEffect(shouldComposeSettings) {
        if (!shouldComposeSettings) {
            backGestureEdge = null
        }
    }

    if (predictiveBackEnabled) {
        PredictiveBackHandler(enabled = showDeviceSettings || connectingMacAddress != null) { progress ->
            try {
                progress.collect { backEvent ->
                    if (latestShowDeviceSettings) {
                        backGestureEdge = backEvent.swipeEdge
                        settingsReveal.snapTo((1f - backEvent.progress).coerceIn(0f, 1f))
                    }
                }
                if (latestShowDeviceSettings) {
                    viewModel.closeDeviceSettings()
                } else if (latestConnectingMac != null) {
                    viewModel.stopDeviceService()
                }
            } catch (_: CancellationException) {
                if (latestShowDeviceSettings) {
                    coroutineScope.launch {
                        if (slideTransitionsEnabled) {
                            settingsReveal.animateTo(1f, SettingsRevealSpringSpec)
                        } else {
                            settingsReveal.snapTo(1f)
                        }
                        backGestureEdge = null
                    }
                }
            }
        }
    } else {
        BackHandler(enabled = showDeviceSettings || connectingMacAddress != null) {
            if (showDeviceSettings) {
                viewModel.closeDeviceSettings()
            } else if (connectingMacAddress != null) {
                viewModel.stopDeviceService()
            }
        }
    }

    val onDeviceSelected = remember(viewModel) {
        { device: PairedDevice -> viewModel.selectDevice(device.macAddress) }
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val reveal = if (hasActiveSettingsState.value) settingsReveal.value else 0f
                    if (slideTransitionsEnabled) {
                        val popExitSign = popExitSignForDirection(
                            backDirection,
                            backGestureEdge,
                            isRtl,
                        )
                        translationX = -(size.width / 4f) * reveal * popExitSign
                    } else {
                        translationX = 0f
                    }
                    alpha = if (reveal >= 0.999f) 0f else 1f
                }
                .then(if (isSelectionCovered) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            DeviceSelectionLayer(
                deviceSettings = activeSettings,
                connectingMacAddress = connectingMacAddress,
                connectedMacAddress = connectedMacAddress,
                onDeviceSelected = onDeviceSelected,
                onDisconnectDevice = viewModel::stopDeviceService,
            )
        }

        if (shouldComposeSettings && activeSettings != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val reveal = settingsReveal.value
                        if (slideTransitionsEnabled) {
                            val popExitSign = popExitSignForDirection(
                                backDirection,
                                backGestureEdge,
                                isRtl,
                            )
                            translationX = size.width * (1f - reveal) * popExitSign
                            alpha = 1f
                        } else {
                            translationX = 0f
                            alpha = reveal
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
            ) {
                DeviceSettingsOverlay(
                    connectionStatus = connectionStatus,
                    currentSettings = activeSettings,
                    onBack = viewModel::closeDeviceSettings,
                    onDisconnect = viewModel::stopDeviceService,
                )
            }
        }
    }
}

@Composable
private fun DeviceSelectionLayer(
    deviceSettings: DeviceSettingsManager?,
    connectingMacAddress: String?,
    connectedMacAddress: String?,
    onDeviceSelected: (PairedDevice) -> Unit,
    onDisconnectDevice: () -> Unit,
) {
    val batteryFlow = remember(deviceSettings, connectedMacAddress) {
        if (deviceSettings != null && connectedMacAddress != null) {
            deviceSettings.allSettingsFlow
                .map { ConnectedBatteryInfo.fromSettings(it.toMap()) }
                .distinctUntilChanged()
        } else {
            flowOf(null)
        }
    }
    val initialBattery = remember(deviceSettings, connectedMacAddress) {
        if (deviceSettings != null && connectedMacAddress != null) {
            ConnectedBatteryInfo.fromSettings(deviceSettings.allSettingsFlow.value.toMap())
        } else {
            null
        }
    }
    val connectedBattery by batteryFlow.collectAsState(initial = initialBattery)

    DeviceSelectionScreen(
        connectingMacAddress = connectingMacAddress,
        connectedMacAddress = connectedMacAddress,
        connectedBattery = connectedBattery,
        onDeviceSelected = onDeviceSelected,
        onDisconnectDevice = onDisconnectDevice,
    )
}

@Composable
private fun DeviceSettingsOverlay(
    connectionStatus: ConnectionStatus,
    currentSettings: DeviceSettingsManager,
    onBack: () -> Unit,
    onDisconnect: () -> Unit,
) {
    DeviceSettingsScreen(
        connectionStatus = connectionStatus,
        onBack = onBack,
        onDisconnect = onDisconnect,
        setSettingValues = currentSettings::setSettingValues,
        allSettingsFlow = currentSettings.allSettingsFlow,
        categoryIdsFlow = currentSettings.categoryIdsFlow,
        getSettingsInCategoryFlow = currentSettings::getSettingsInCategoryFlow,
        quickPresetSlotsFlow = currentSettings.quickPresetSlots,
        onQuickPresetSlotChange = currentSettings::setQuickPresetSlot,
        quickPresetsFlow = currentSettings.quickPresetsFlow,
        activateQuickPreset = currentSettings::activateQuickPreset,
        createQuickPreset = currentSettings::createQuickPreset,
        deleteQuickPreset = currentSettings::deleteQuickPreset,
        toggleQuickPresetSetting = currentSettings::toggleQuickPresetSetting,
        featuredSettingSlotsFlow = currentSettings.featuredSettingSlots,
        onFeaturedSettingSlotChange = currentSettings::setFeaturedSettingSlot,
        onQuickPresetLoadCurrentSettings = currentSettings::createQuickPreset,
        legacyEqualizerProfilesFlow = currentSettings.legacyEqualizerProfilesFlow,
        onMigrateLegacyEqualizerProfile = currentSettings::migrateLegacyEqualizerProfile,
    )
}
