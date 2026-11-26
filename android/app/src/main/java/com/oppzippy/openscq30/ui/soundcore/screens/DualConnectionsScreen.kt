package com.oppzippy.openscq30.ui.soundcore.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.MultiSelectWithRemoveCommandInner
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitch
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DualConnectionsScreen(
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
) {
    val dualEnabled = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.DUAL_CONNECTIONS)
    val devicesSetting =
        settings.getSetting<Setting.MultiSelectWithRemoveSetting>(SoundcoreSettingIds.DUAL_CONNECTIONS_DEVICES)
    val hasConnectNewDevice = settings.containsKey(SoundcoreSettingIds.DUAL_CONNECTIONS_CONNECT_NEW_DEVICE)

    val connectedSet = devicesSetting?.values?.toSet().orEmpty()
    val allDevices = devicesSetting?.let {
        it.setting.options.zip(it.setting.localizedOptions)
    }.orEmpty()
    val currentDevices = allDevices.filter { (mac, _) -> mac in connectedSet }
    val historyDevices = allDevices.filter { (mac, _) -> mac !in connectedSet }

    var manageHistory by remember { mutableStateOf(false) }
    var deviceToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }
    var deviceToDisconnectForPairing by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showPairingActiveDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    deviceToRemove?.let { (mac, name) ->
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { deviceToRemove = null },
            title = {
                Text(
                    text = stringResource(R.string.delete),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_confirm, name),
                    color = SoundcoreColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetSetting(
                            SoundcoreSettingIds.DUAL_CONNECTIONS_DEVICES,
                            MultiSelectWithRemoveCommandInner.Remove(mac).toValue(),
                        )
                        deviceToRemove = null
                    },
                ) {
                    Text(stringResource(R.string.delete), color = SoundcoreColors.Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToRemove = null }) {
                    Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
                }
            },
        )
    }

    deviceToDisconnectForPairing?.let { (mac, name) ->
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { deviceToDisconnectForPairing = null },
            title = {
                Text(
                    text = stringResource(R.string.connect_new_device_disconnect_title),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.connect_new_device_disconnect_message, name),
                    color = SoundcoreColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = connectedSet.toMutableSet().apply { remove(mac) }
                        val options = devicesSetting?.setting?.options.orEmpty()
                        onSetSetting(
                            SoundcoreSettingIds.DUAL_CONNECTIONS_DEVICES,
                            options.filter { it in updated }.toValue(),
                        )
                        deviceToDisconnectForPairing = null
                        coroutineScope.launch {
                            delay(600)
                            onSetSetting(
                                SoundcoreSettingIds.DUAL_CONNECTIONS_CONNECT_NEW_DEVICE,
                                true.toValue(),
                            )
                            showPairingActiveDialog = true
                        }
                    },
                ) {
                    Text(stringResource(R.string.disconnect_and_pair), color = SoundcoreColors.Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDisconnectForPairing = null }) {
                    Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
                }
            },
        )
    }

    if (showPairingActiveDialog) {
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { showPairingActiveDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.pairing_mode_started),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.pairing_mode_started_description),
                    color = SoundcoreColors.TextSecondary,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showPairingActiveDialog = false }) {
                    Text(stringResource(R.string.ok), color = SoundcoreColors.Cyan)
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(R.string.device_list),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (dualEnabled != null) {
                SoundcoreCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.dual_connections),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        SoundcoreSwitch(
                            checked = dualEnabled.value,
                            onCheckedChange = {
                                onSetSetting(SoundcoreSettingIds.DUAL_CONNECTIONS, it.toValue())
                            },
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.dual_connections_description),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            if (devicesSetting != null) {
                if (currentDevices.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.current),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    SoundcoreGroupedCard {
                        currentDevices.forEachIndexed { idx, (mac, name) ->
                            DualDeviceRow(
                                name = name,
                                isConnected = true,
                                showActiveBars = idx == 0,
                                showRemove = false,
                                onToggle = { checked ->
                                    val updated = connectedSet.toMutableSet().apply {
                                        if (checked) add(mac) else remove(mac)
                                    }
                                    onSetSetting(
                                        SoundcoreSettingIds.DUAL_CONNECTIONS_DEVICES,
                                        devicesSetting.setting.options.filter { it in updated }.toValue(),
                                    )
                                },
                                onRemove = {},
                            )
                            if (idx < currentDevices.lastIndex) {
                                SoundcoreDivider()
                            }
                        }
                    }
                }

                if (historyDevices.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.history),
                            color = SoundcoreColors.TextSecondary,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = if (manageHistory) {
                                stringResource(R.string.done)
                            } else {
                                stringResource(R.string.manage)
                            },
                            color = SoundcoreColors.Cyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { manageHistory = !manageHistory },
                        )
                    }

                    SoundcoreGroupedCard {
                        historyDevices.forEachIndexed { idx, (mac, name) ->
                            DualDeviceRow(
                                name = name,
                                isConnected = false,
                                showActiveBars = false,
                                showRemove = manageHistory,
                                canToggleOn = connectedSet.size < 2,
                                onToggle = { checked ->
                                    if (!checked || connectedSet.size < 2) {
                                        val updated = connectedSet.toMutableSet().apply {
                                            if (checked) add(mac) else remove(mac)
                                        }
                                        onSetSetting(
                                            SoundcoreSettingIds.DUAL_CONNECTIONS_DEVICES,
                                            devicesSetting.setting.options.filter { it in updated }.toValue(),
                                        )
                                    }
                                },
                                onRemove = { deviceToRemove = mac to name },
                            )
                            if (idx < historyDevices.lastIndex) {
                                SoundcoreDivider()
                            }
                        }
                    }
                }

                if (allDevices.isEmpty()) {
                    SoundcoreCard {
                        Text(
                            text = stringResource(R.string.no_devices_found),
                            color = SoundcoreColors.TextSecondary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        if (hasConnectNewDevice) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SoundcoreColors.Cyan, SoundcoreColors.CyanBright),
                            ),
                        )
                        .clickable {
                            if (connectedSet.size >= 2) {
                                deviceToDisconnectForPairing = currentDevices.last()
                            } else {
                                onSetSetting(
                                    SoundcoreSettingIds.DUAL_CONNECTIONS_CONNECT_NEW_DEVICE,
                                    true.toValue(),
                                )
                                showPairingActiveDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.connect_new_device),
                        color = SoundcoreColors.OnAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DualDeviceRow(
    name: String,
    isConnected: Boolean,
    showActiveBars: Boolean,
    showRemove: Boolean,
    canToggleOn: Boolean = true,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showActiveBars) {
            Icon(
                painter = painterResource(R.drawable.graphic_eq_24px),
                contentDescription = null,
                tint = SoundcoreColors.TextPrimary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = name,
            color = SoundcoreColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (showRemove && !isConnected) {
            IconButton(onClick = onRemove) {
                Icon(
                    painter = painterResource(R.drawable.remove_24px),
                    contentDescription = stringResource(R.string.delete),
                    tint = SoundcoreColors.TextSecondary,
                )
            }
        } else {
            SoundcoreSwitch(
                checked = isConnected,
                enabled = isConnected || canToggleOn,
                onCheckedChange = onToggle,
            )
        }
    }
}
