package com.oppzippy.openscq30.ui.devicesettings.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.oppzippy.openscq30.lib.bindings.translateSettingId
import com.oppzippy.openscq30.lib.bindings.translateValue
import com.oppzippy.openscq30.lib.wrapper.QuickPreset
import com.oppzippy.openscq30.lib.wrapper.QuickPresetField
import com.oppzippy.openscq30.lib.wrapper.Select
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitch
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun QuickPresetsScreen(
    quickPresets: List<QuickPreset>,
    onActivate: (String) -> Unit,
    onCreate: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        if (onBack != null) {
            SoundcoreTopBar(
                title = stringResource(R.string.quick_presets),
                onBack = onBack,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            QuickPresetsList(
                quickPresets = quickPresets,
                onActivate = onActivate,
                onEdit = onEdit,
                onDelete = onDelete,
            )
            CreateQuickPresetFloatingButton(
                onCreate = onCreate,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun QuickPresetsList(
    quickPresets: List<QuickPreset>,
    onActivate: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var deleteDialog by remember { mutableStateOf<String?>(null) }
    deleteDialog?.let { currentDeleteDialog ->
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { deleteDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(currentDeleteDialog)
                        deleteDialog = null
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = null }) {
                    Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_quick_preset),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_confirm, currentDeleteDialog),
                    color = SoundcoreColors.TextSecondary,
                )
            },
        )
    }

    if (quickPresets.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = quickPresets,
                key = { preset -> preset.name },
            ) { preset ->
                SoundcoreCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = preset.name,
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SoundcoreColors.Cyan)
                                    .clickable { onActivate(preset.name) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.activate),
                                    color = SoundcoreColors.OnAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SoundcoreColors.SurfacePill)
                                    .clickable { onEdit(preset.name) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.edit),
                                    color = SoundcoreColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            IconButton(onClick = { deleteDialog = preset.name }) {
                                Icon(
                                    painter = painterResource(R.drawable.remove_24px),
                                    contentDescription = stringResource(R.string.delete),
                                    tint = SoundcoreColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
            item(key = "bottom_spacer") {
                Spacer(Modifier.height(80.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_quick_presets),
                color = SoundcoreColors.TextSecondary,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun CreateQuickPresetFloatingButton(
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogShown by remember { mutableStateOf(false) }
    if (isDialogShown) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { isDialogShown = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDialogShown = false
                        onCreate(name)
                    },
                ) {
                    Text(stringResource(R.string.create), color = SoundcoreColors.Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogShown = false }) {
                    Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.create_quick_preset),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                )
            },
        )
    }

    FloatingActionButton(
        modifier = modifier,
        onClick = { isDialogShown = true },
        shape = CircleShape,
        containerColor = SoundcoreColors.Cyan,
        contentColor = SoundcoreColors.OnAccent,
    ) {
        Icon(
            painter = painterResource(R.drawable.add_24px),
            contentDescription = stringResource(R.string.create),
        )
    }
}

@Composable
fun EditQuickPresetPage(
    settings: Map<String, Setting>,
    quickPreset: QuickPreset,
    onToggleSetting: (name: String, setting: String, isEnabled: Boolean) -> Unit,
    onLoadCurrentSettings: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var isInfoDialogOpen by remember { mutableStateOf(false) }
    if (isInfoDialogOpen) {
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { isInfoDialogOpen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { isInfoDialogOpen = false }) {
                    Text(stringResource(R.string.close), color = SoundcoreColors.Cyan)
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.load_current_settings),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.load_current_settings_help),
                    color = SoundcoreColors.TextSecondary,
                )
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        if (onBack != null) {
            SoundcoreTopBar(
                title = quickPreset.name,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { isInfoDialogOpen = true }) {
                        Icon(
                            painter = painterResource(R.drawable.info_24px),
                            contentDescription = stringResource(R.string.information),
                            tint = SoundcoreColors.TextPrimary,
                        )
                    }
                },
            )
        }

        val configuration = LocalConfiguration.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "load_current_settings") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SoundcoreColors.Cyan)
                        .clickable(onClick = onLoadCurrentSettings)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.load_current_settings),
                        color = SoundcoreColors.OnAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            items(
                items = quickPreset.fields,
                key = { field -> field.settingId },
            ) { field ->
                val settingTitle = remember(field.settingId, configuration) {
                    translateSettingId(field.settingId)
                }
                val currentSetting = settings[field.settingId]
                val settingValueLabel = remember(currentSetting, field.value, configuration) {
                    translateValue(currentSetting, field.value)
                }
                SoundcoreCard(
                    onClick = { onToggleSetting(quickPreset.name, field.settingId, !field.isEnabled) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settingTitle,
                                color = SoundcoreColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = settingValueLabel,
                                color = SoundcoreColors.TextSecondary,
                                fontSize = 13.sp,
                            )
                        }
                        SoundcoreSwitch(
                            checked = field.isEnabled,
                            onCheckedChange = null,
                        )
                    }
                }
            }
            item(key = "bottom_spacer") {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewQuickPresetsList() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        QuickPresetsScreen(
            quickPresets = listOf(
                QuickPreset(name = "Preset 1", fields = emptyList()),
                QuickPreset(name = "Preset 2", fields = emptyList()),
                QuickPreset(name = "Preset 3", fields = emptyList()),
            ),
            onEdit = {},
            onActivate = {},
            onCreate = {},
            onDelete = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEditQuickPresetPage() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        EditQuickPresetPage(
            settings = mapOf(
                Pair(
                    "test",
                    Setting.SelectSetting(
                        setting = Select(
                            options = listOf("a", "b", "c"),
                            localizedOptions = listOf("A", "B", "C"),
                        ),
                        value = "b",
                    ),
                ),
            ),
            quickPreset = QuickPreset(
                name = "Preset 1",
                fields = listOf(QuickPresetField("test", "b".toValue(), true)),
            ),
            onToggleSetting = { _, _, _ -> },
            onLoadCurrentSettings = {},
            onBack = {},
        )
    }
}
