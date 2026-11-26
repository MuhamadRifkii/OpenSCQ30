package com.oppzippy.openscq30.ui.soundcore.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateSettingId
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreNavRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitchRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

private data class CycleModeItem(
    val settingId: String,
    val labelRes: Int,
    val iconRes: Int,
    val checked: Boolean,
)

@Composable
fun ControlsScreen(
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
) {
    val leftIds = remember {
        listOf(
            SoundcoreSettingIds.LEFT_SINGLE_PRESS to R.string.single_tap,
            SoundcoreSettingIds.LEFT_DOUBLE_PRESS to R.string.double_tap,
            SoundcoreSettingIds.LEFT_TRIPLE_PRESS to R.string.triple_tap,
            SoundcoreSettingIds.LEFT_LONG_PRESS to R.string.press_and_hold,
        )
    }
    val rightIds = remember {
        listOf(
            SoundcoreSettingIds.RIGHT_SINGLE_PRESS to R.string.single_tap,
            SoundcoreSettingIds.RIGHT_DOUBLE_PRESS to R.string.double_tap,
            SoundcoreSettingIds.RIGHT_TRIPLE_PRESS to R.string.triple_tap,
            SoundcoreSettingIds.RIGHT_LONG_PRESS to R.string.press_and_hold,
        )
    }

    val ncCycle = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.NOISE_CANCELING_MODE_IN_CYCLE)
    val normalCycle = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.NORMAL_MODE_IN_CYCLE)
    val transparencyCycle = settings.getSetting<Setting.ToggleSetting>(
        SoundcoreSettingIds.TRANSPARENCY_MODE_IN_CYCLE,
    )
    val cycleModes = remember(ncCycle, normalCycle, transparencyCycle) {
        buildList {
            if (ncCycle != null) {
                add(
                    CycleModeItem(
                        settingId = SoundcoreSettingIds.NOISE_CANCELING_MODE_IN_CYCLE,
                        labelRes = R.string.noise_cancellation,
                        iconRes = R.drawable.noise_control_24px,
                        checked = ncCycle.value,
                    ),
                )
            }
            if (normalCycle != null) {
                add(
                    CycleModeItem(
                        settingId = SoundcoreSettingIds.NORMAL_MODE_IN_CYCLE,
                        labelRes = R.string.normal_mode,
                        iconRes = R.drawable.person_24px,
                        checked = normalCycle.value,
                    ),
                )
            }
            if (transparencyCycle != null) {
                add(
                    CycleModeItem(
                        settingId = SoundcoreSettingIds.TRANSPARENCY_MODE_IN_CYCLE,
                        labelRes = R.string.transparency_mode,
                        iconRes = R.drawable.speaker_24px,
                        checked = transparencyCycle.value,
                    ),
                )
            }
        }
    }
    val onToggleCycleMode: (String, Boolean) -> Unit = { id, nextChecked ->
        val enabledCount = cycleModes.count { it.checked }
        if (nextChecked || enabledCount > 1) {
            onSetSetting(id, nextChecked.toValue())
        }
    }

    val hasReset = settings.containsKey(SoundcoreSettingIds.RESET_BUTTONS_TO_DEFAULT)

    var activePickerSettingId by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    activePickerSettingId?.let { settingId ->
        val setting = settings[settingId]
        val options: List<String>
        val localizedOptions: List<String>
        val currentValue: String?
        val allowNone: Boolean

        when (setting) {
            is Setting.OptionalSelectSetting -> {
                options = setting.setting.options
                localizedOptions = setting.setting.localizedOptions
                currentValue = setting.value
                allowNone = true
            }

            is Setting.SelectSetting -> {
                options = setting.setting.options
                localizedOptions = setting.setting.localizedOptions
                currentValue = setting.value
                allowNone = false
            }

            else -> {
                options = emptyList()
                localizedOptions = emptyList()
                currentValue = null
                allowNone = false
            }
        }

        if (options.isNotEmpty()) {
            ButtonActionPickerDialog(
                title = translateSettingId(settingId),
                options = options,
                localizedOptions = localizedOptions,
                selectedOption = currentValue,
                allowNone = allowNone,
                cycleModes = cycleModes,
                onToggleCycleMode = onToggleCycleMode,
                onSelect = { selected ->
                    onSetSetting(settingId, selected.toValue())
                    activePickerSettingId = null
                },
                onDismiss = { activePickerSettingId = null },
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { showResetConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.reset_controls),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.reset_controls_confirm),
                    color = SoundcoreColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetSetting(SoundcoreSettingIds.RESET_BUTTONS_TO_DEFAULT, true.toValue())
                        showResetConfirm = false
                    },
                ) {
                    Text(stringResource(R.string.confirm), color = SoundcoreColors.Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
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
            title = stringResource(R.string.controls),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EarbudControlSection(
                badge = "L",
                title = stringResource(R.string.left_earbud),
                rows = leftIds,
                settings = settings,
                onSelectRow = { activePickerSettingId = it },
            )

            EarbudControlSection(
                badge = "R",
                title = stringResource(R.string.right_earbud),
                rows = rightIds,
                settings = settings,
                onSelectRow = { activePickerSettingId = it },
            )

            if (cycleModes.isNotEmpty()) {
                SoundcoreGroupedCard {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 6.dp,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.sound_mode_cycle),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.ambient_sound_cycle_description),
                            color = SoundcoreColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                    }
                    cycleModes.forEachIndexed { idx, mode ->
                        SoundcoreSwitchRow(
                            title = stringResource(mode.labelRes),
                            checked = mode.checked,
                            onCheckedChange = { onToggleCycleMode(mode.settingId, it) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(mode.iconRes),
                                    contentDescription = null,
                                    tint = if (mode.checked) {
                                        SoundcoreColors.TextPrimary
                                    } else {
                                        SoundcoreColors.TextSecondary
                                    },
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                        )
                        if (idx < cycleModes.lastIndex) {
                            SoundcoreDivider()
                        }
                    }
                }
            }

            if (hasReset) {
                SoundcoreCard(onClick = { showResetConfirm = true }) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.reset_controls),
                            color = SoundcoreColors.Cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EarbudControlSection(
    badge: String,
    title: String,
    rows: List<Pair<String, Int>>,
    settings: Map<String, Setting>,
    onSelectRow: (String) -> Unit,
) {
    val availableRows = rows.filter { settings.containsKey(it.first) }
    if (availableRows.isEmpty()) return

    SoundcoreGroupedCard {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.3.dp, SoundcoreColors.Cyan, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = SoundcoreColors.Cyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = SoundcoreColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        availableRows.forEachIndexed { index, (settingId, labelRes) ->
            val currentText = resolveButtonActionLabel(settings[settingId])
            SoundcoreNavRow(
                title = stringResource(labelRes),
                valueText = currentText,
                onClick = { onSelectRow(settingId) },
            )
            if (index < availableRows.lastIndex) {
                SoundcoreDivider()
            }
        }
    }
}

@Composable
private fun resolveButtonActionLabel(setting: Setting?): String = when (setting) {
    is Setting.OptionalSelectSetting -> {
        val idx = setting.value?.let { setting.setting.options.indexOf(it) } ?: -1
        if (idx >= 0) {
            setting.setting.localizedOptions.getOrElse(idx) { setting.value ?: "" }
        } else {
            stringResource(R.string.none)
        }
    }

    is Setting.SelectSetting -> {
        val idx = setting.setting.options.indexOf(setting.value)
        setting.setting.localizedOptions.getOrElse(idx) { setting.value }
    }

    else -> stringResource(R.string.none)
}

@Composable
private fun ButtonActionPickerDialog(
    title: String,
    options: List<String>,
    localizedOptions: List<String>,
    selectedOption: String?,
    allowNone: Boolean,
    cycleModes: List<CycleModeItem>,
    onToggleCycleMode: (String, Boolean) -> Unit,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = SoundcoreColors.SurfaceCard,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = SoundcoreColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { idx, opt ->
                    val isSelected = opt == selectedOption
                    val label = localizedOptions.getOrElse(idx) { opt }
                    val showCycleModes = opt == "AmbientSoundMode" && cycleModes.isNotEmpty()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(opt) }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SoundcoreColors.Cyan else SoundcoreColors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check_24px),
                                    contentDescription = stringResource(R.string.selected),
                                    tint = SoundcoreColors.Cyan,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        if (showCycleModes) {
                            Text(
                                text = stringResource(R.string.ambient_sound_cycle_description),
                                color = SoundcoreColors.TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
                            )
                            cycleModes.forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onToggleCycleMode(mode.settingId, !mode.checked) }
                                        .padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = mode.checked,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SoundcoreColors.Cyan,
                                            checkmarkColor = SoundcoreColors.OnAccent,
                                            uncheckedColor = SoundcoreColors.TextSecondary,
                                        ),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        painter = painterResource(mode.iconRes),
                                        contentDescription = null,
                                        tint = if (mode.checked) {
                                            SoundcoreColors.TextPrimary
                                        } else {
                                            SoundcoreColors.TextSecondary
                                        },
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(mode.labelRes),
                                        color = if (mode.checked) {
                                            SoundcoreColors.TextPrimary
                                        } else {
                                            SoundcoreColors.TextSecondary
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = if (mode.checked) FontWeight.Medium else FontWeight.Normal,
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    if (idx < options.lastIndex || allowNone) {
                        HorizontalDivider(color = SoundcoreColors.Divider, thickness = 0.5.dp)
                    }
                }
                if (allowNone) {
                    val isSelected = selectedOption == null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(null) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.none),
                            color = if (isSelected) SoundcoreColors.Cyan else SoundcoreColors.TextPrimary,
                            fontSize = 15.sp,
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = stringResource(R.string.selected),
                                tint = SoundcoreColors.Cyan,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = SoundcoreColors.TextSecondary)
            }
        },
    )
}
