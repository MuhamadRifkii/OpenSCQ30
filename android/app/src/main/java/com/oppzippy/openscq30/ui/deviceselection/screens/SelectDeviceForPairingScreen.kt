package com.oppzippy.openscq30.ui.deviceselection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.ConnectionDescriptor
import com.oppzippy.openscq30.ui.deviceselection.components.AddDeviceCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitchRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.utils.Loading

@Composable
fun SelectDeviceForPairingScreen(
    model: String,
    isDemoMode: Boolean,
    devices: List<ConnectionDescriptor>?,
    onDemoModeChange: (Boolean) -> Unit,
    onDescriptorSelected: (ConnectionDescriptor) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(id = R.string.select_x, translateDeviceModel(model)),
            onBack = onBackClick,
        )

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SoundcoreGroupedCard {
                    SoundcoreSwitchRow(
                        title = stringResource(R.string.demo_mode),
                        checked = isDemoMode,
                        onCheckedChange = onDemoModeChange,
                    )
                }
            }
            if (devices != null) {
                items(devices) { descriptor ->
                    AddDeviceCard(
                        modifier = Modifier.clickable { onDescriptorSelected(descriptor) },
                        name = descriptor.name,
                        macAddress = descriptor.macAddress,
                    )
                }
            } else {
                item {
                    Loading()
                }
            }
        }
    }
}
