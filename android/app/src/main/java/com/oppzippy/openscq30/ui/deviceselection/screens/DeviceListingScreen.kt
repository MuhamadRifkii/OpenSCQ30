package com.oppzippy.openscq30.ui.deviceselection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.ui.deviceselection.components.ConnectToDeviceCard
import com.oppzippy.openscq30.ui.soundcore.components.ConnectedBatteryInfo
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.utils.Loading
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListingScreen(
    devices: List<PairedDevice>?,
    connectingMacAddress: String? = null,
    connectedMacAddress: String? = null,
    connectedBattery: ConnectedBatteryInfo? = null,
    onRefreshClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onDeviceClick: (PairedDevice) -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onUnpair: (PairedDevice) -> Unit = {},
    onAddDeviceClick: () -> Unit = {},
) {
    Scaffold(
        containerColor = SoundcoreColors.Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoundcoreColors.Background,
                    titleContentColor = SoundcoreColors.TextPrimary,
                    actionIconContentColor = SoundcoreColors.TextPrimary,
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.my_devices),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onInfoClick) {
                        Icon(
                            painter = painterResource(R.drawable.info_24px),
                            contentDescription = stringResource(id = R.string.info),
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(R.drawable.settings_24px),
                            contentDescription = stringResource(id = R.string.settings),
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            val pullToRefreshState = rememberPullToRefreshState()
            val scope = rememberCoroutineScope()
            PullToRefreshBox(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                isRefreshing = false,
                onRefresh = {
                    onRefreshClick()
                    scope.launch {
                        pullToRefreshState.animateToHidden()
                    }
                },
                state = pullToRefreshState,
            ) {
                if (devices == null) {
                    Loading()
                } else {
                    DeviceList(
                        devices = devices,
                        connectingMacAddress = connectingMacAddress,
                        connectedMacAddress = connectedMacAddress,
                        connectedBattery = connectedBattery,
                        modifier = Modifier.fillMaxSize(),
                        onDeviceClick = onDeviceClick,
                        onDisconnectClick = onDisconnectClick,
                        onUnpair = onUnpair,
                        onAddDeviceClick = onAddDeviceClick,
                    )
                }
            }
        },
    )
}

@Composable
private fun DeviceList(
    devices: List<PairedDevice>,
    connectingMacAddress: String? = null,
    connectedMacAddress: String? = null,
    connectedBattery: ConnectedBatteryInfo? = null,
    modifier: Modifier = Modifier,
    onDeviceClick: (device: PairedDevice) -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    onUnpair: (device: PairedDevice) -> Unit = {},
    onAddDeviceClick: () -> Unit = {},
) {
    var deviceToUnpair: PairedDevice? by remember { mutableStateOf(null) }

    deviceToUnpair?.let { device ->
        val isDeviceConnected = connectedMacAddress != null &&
            device.macAddress.equals(connectedMacAddress, ignoreCase = true)
        AlertDialog(
            onDismissRequest = { deviceToUnpair = null },
            title = {
                Text(
                    if (isDeviceConnected) {
                        translateDeviceModel(device.model)
                    } else {
                        stringResource(id = R.string.unpair_device)
                    },
                )
            },
            text = {
                Text(
                    if (isDeviceConnected) {
                        device.macAddress
                    } else {
                        stringResource(
                            id = R.string.unpairing_from_device_name,
                            translateDeviceModel(device.model),
                        )
                    },
                )
            },
            dismissButton = {
                if (isDeviceConnected) {
                    TextButton(
                        onClick = {
                            onDisconnectClick()
                            deviceToUnpair = null
                        },
                    ) {
                        Text(stringResource(R.string.disconnect))
                    }
                } else {
                    TextButton(onClick = { deviceToUnpair = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeviceConnected) {
                            onDisconnectClick()
                        }
                        onUnpair(device)
                        deviceToUnpair = null
                    },
                ) {
                    Text(stringResource(R.string.unpair))
                }
            },
        )
    }

    val configuration = LocalConfiguration.current
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = true,
    ) {
        if (devices.isEmpty()) {
            item(key = "no_devices_found") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.no_devices_found),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 15.sp,
                    )
                }
            }
        } else {
            items(
                items = devices,
                key = { device -> device.macAddress },
            ) { device ->
                val isConnecting = connectingMacAddress != null &&
                    device.macAddress.equals(connectingMacAddress, ignoreCase = true)
                val isConnected = connectedMacAddress != null &&
                    device.macAddress.equals(connectedMacAddress, ignoreCase = true)
                val deviceName = remember(device.model, configuration) {
                    translateDeviceModel(device.model)
                }
                ConnectToDeviceCard(
                    modifier = Modifier.combinedClickable(
                        enabled = connectingMacAddress == null,
                        onClick = { onDeviceClick(device) },
                        onLongClick = { deviceToUnpair = device },
                    ),
                    model = device.model,
                    name = deviceName,
                    macAddress = device.macAddress,
                    isDemo = device.isDemo,
                    isConnecting = isConnecting,
                    isConnected = isConnected,
                    connectedBattery = if (isConnected) connectedBattery else null,
                )
            }
        }

        item(key = "add_device_action") {
            AddDeviceActionCard(onClick = onAddDeviceClick)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddDeviceActionCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SoundcoreColors.SurfaceCard)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.add_24px),
                contentDescription = stringResource(R.string.add),
                tint = SoundcoreColors.TextPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_device),
                color = SoundcoreColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDeviceList() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        val devices = listOf(
            PairedDevice("SoundcoreD1202C", "00:11:22:33:44:55", isDemo = false),
            PairedDevice("SoundcoreA3959", "00:11:22:33:44:66", isDemo = true),
        )
        DeviceListingScreen(devices)
    }
}
