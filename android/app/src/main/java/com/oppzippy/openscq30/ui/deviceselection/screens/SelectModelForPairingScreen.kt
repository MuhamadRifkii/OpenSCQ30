package com.oppzippy.openscq30.ui.deviceselection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.deviceModels
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.ui.deviceselection.components.AddDeviceModelCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun SelectModelForPairingScreen(onModelSelected: (String) -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(id = R.string.select_device_model),
            onBack = onBackClick,
        )

        var searchQuery by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SoundcoreColors.SurfaceCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.search_24px),
                contentDescription = stringResource(R.string.search),
                tint = SoundcoreColors.TextSecondary,
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = SoundcoreColors.TextPrimary,
                        fontSize = 16.sp,
                    ),
                    cursorBrush = SolidColor(SoundcoreColors.Cyan),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val configuration = LocalConfiguration.current
        val allDeviceModels = remember(configuration) {
            deviceModels().map { model -> model to translateDeviceModel(model) }
        }
        val filteredDeviceModels = remember(searchQuery, allDeviceModels) {
            if (searchQuery.isEmpty()) {
                allDeviceModels
            } else {
                allDeviceModels.filter { (model, name) ->
                    model.contains(searchQuery, ignoreCase = true) ||
                        name.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .testTag("modelList")
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (filteredDeviceModels.isNotEmpty()) {
                items(
                    items = filteredDeviceModels,
                    key = { (model, _) -> model },
                ) { (model, name) ->
                    AddDeviceModelCard(
                        modifier = Modifier.clickable { onModelSelected(model) },
                        name = name,
                        model = model,
                    )
                }
            } else {
                item(key = "no_items_found") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.no_items_found),
                            color = SoundcoreColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
