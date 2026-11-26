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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.ConnectedBatteryInfo
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreBatteryStrip
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreEarbudsArtwork
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitch
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.utils.throttledState
import kotlin.math.roundToInt

@Composable
fun SoundcoreDashboardScreen(
    deviceModel: String,
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
    onOpenSoundEffects: () -> Unit,
    onOpenControls: () -> Unit,
    onOpenMoreSettings: () -> Unit,
    onOpenQuickPresets: () -> Unit,
) {
    val shortModelName = translateDeviceModel(deviceModel).removePrefix("Soundcore ").ifBlank {
        translateDeviceModel(deviceModel)
    }

    val ldacSetting = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.LDAC)
    val batteryInfo = remember(settings) { ConnectedBatteryInfo.fromSettings(settings) }

    val presetEq = settings.getSetting<Setting.PresetEqualizerProfileSelect>(
        SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
    )
    val customEq = settings.getSetting<Setting.ModifiableSelectSetting>(
        SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
    )

    val activeSoundEffectName = remember(presetEq, customEq) {
        val presetIdx = presetEq?.value?.let { presetEq.select.options.indexOf(it) } ?: -1
        if (presetIdx >= 0) {
            presetEq?.select?.localizedOptions?.getOrNull(presetIdx)
        } else {
            customEq?.value
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            onBack = onBack,
            actions = {
                IconButton(onClick = onOpenQuickPresets) {
                    Icon(
                        painter = painterResource(R.drawable.bolt_24px),
                        contentDescription = stringResource(R.string.quick_presets),
                        tint = SoundcoreColors.TextPrimary,
                    )
                }
                IconButton(onClick = onOpenMoreSettings) {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = stringResource(R.string.more_settings),
                        tint = SoundcoreColors.TextPrimary,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SoundcoreEarbudsArtwork(
                modifier = Modifier.size(160.dp),
                model = deviceModel,
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = shortModelName,
                    color = SoundcoreColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (ldacSetting?.value == true) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, SoundcoreColors.TextSecondary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = "LDAC",
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }

            if (batteryInfo.hasAny) {
                Spacer(Modifier.height(14.dp))
                SoundcoreBatteryStrip(
                    battery = batteryInfo,
                    modifier = Modifier.fillMaxWidth(),
                    compact = false,
                )
            }

            Spacer(Modifier.height(22.dp))

            AmbientSoundCard(
                settings = settings,
                onSetSetting = onSetSetting,
            )

            Spacer(Modifier.height(14.dp))

            if (presetEq != null || customEq != null) {
                SoundcoreCard(onClick = onOpenSoundEffects) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.equalizer_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sound_effects),
                                color = SoundcoreColors.TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            if (!activeSoundEffectName.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = activeSoundEffectName,
                                    color = SoundcoreColors.TextSecondary,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            if (settings.containsKey(SoundcoreSettingIds.LEFT_DOUBLE_PRESS) ||
                settings.containsKey(SoundcoreSettingIds.LEFT_LONG_PRESS)
            ) {
                SoundcoreCard(onClick = onOpenControls) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.radio_button_checked_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.controls),
                                color = SoundcoreColors.TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.customize_touch_controls),
                                color = SoundcoreColors.TextSecondary,
                                fontSize = 13.sp,
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AmbientSoundCard(
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
) {
    val ambientSoundMode = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.AMBIENT_SOUND_MODE)
        ?: return
    val noiseCancelingMode = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.NOISE_CANCELING_MODE)
    val manualNoiseCanceling = settings.getSetting<Setting.I32RangeSetting>(SoundcoreSettingIds.MANUAL_NOISE_CANCELING)
    val adaptiveNoiseCanceling =
        settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.ADAPTIVE_NOISE_CANCELING)
    val multiSceneAnc = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.MULTI_SCENE_NOISE_CANCELING)
    val adaptiveAncSensitivity =
        settings.getSetting<Setting.I32RangeSetting>(SoundcoreSettingIds.ADAPTIVE_ANC_SENSITIVITY)
    val realTimeAdaptiveAnc = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.REAL_TIME_ADAPTIVE_ANC)
    val transparencyMode = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.TRANSPARENCY_MODE)
    val windNoiseSuppression = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.WIND_NOISE_SUPPRESSION)
    val windNoiseDetected = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.WIND_NOISE_DETECTED)

    var showTransparencyModeDialog by remember { mutableStateOf(false) }
    if (showTransparencyModeDialog && transparencyMode != null) {
        OptionPickerDialog(
            title = stringResource(R.string.transparency_mode),
            options = transparencyMode.setting.options,
            localizedOptions = transparencyMode.setting.localizedOptions,
            selectedOption = transparencyMode.value,
            onSelect = {
                onSetSetting(SoundcoreSettingIds.TRANSPARENCY_MODE, it.toValue())
                showTransparencyModeDialog = false
            },
            onDismiss = { showTransparencyModeDialog = false },
        )
    }

    SoundcoreCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.graphic_eq_24px),
                contentDescription = null,
                tint = SoundcoreColors.TextPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.ambient_sound),
                color = SoundcoreColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.height(18.dp))

        // Soundcore order: Noise Canceling, Normal, Transparency
        val orderedModes = listOf("NoiseCanceling", "Normal", "Transparency")
            .filter { it in ambientSoundMode.setting.options }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            orderedModes.forEach { option ->
                val isSelected = ambientSoundMode.value == option
                val label = when (option) {
                    "NoiseCanceling" -> stringResource(R.string.noise_cancellation)
                    "Normal" -> stringResource(R.string.normal_mode)
                    "Transparency" -> stringResource(R.string.transparency)
                    else -> {
                        val idx = ambientSoundMode.setting.options.indexOf(option)
                        ambientSoundMode.setting.localizedOptions.getOrElse(idx) { option }
                    }
                }
                val iconRes = when (option) {
                    "NoiseCanceling" -> R.drawable.noise_control_24px
                    "Normal" -> R.drawable.person_24px
                    else -> R.drawable.speaker_24px
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSetSetting(SoundcoreSettingIds.AMBIENT_SOUND_MODE, option.toValue()) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SoundcoreColors.Cyan else SoundcoreColors.SurfaceElevated),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = label,
                            tint = if (isSelected) SoundcoreColors.OnAccent else SoundcoreColors.TextSecondary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = if (isSelected) SoundcoreColors.TextPrimary else SoundcoreColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        val currentMode = ambientSoundMode.value

        if (currentMode == "NoiseCanceling") {
            Spacer(Modifier.height(16.dp))
            if (noiseCancelingMode != null) {
                SegmentedOptionRow(
                    options = noiseCancelingMode.setting.options,
                    localizedOptions = noiseCancelingMode.setting.localizedOptions,
                    selectedOption = noiseCancelingMode.value,
                    onSelect = { onSetSetting(SoundcoreSettingIds.NOISE_CANCELING_MODE, it.toValue()) },
                )
            }

            when (noiseCancelingMode?.value) {
                "Manual" -> {
                    manualNoiseCanceling?.let { rangeSetting ->
                        Spacer(Modifier.height(12.dp))
                        ManualAncLevelBar(
                            value = rangeSetting.value,
                            min = rangeSetting.setting.start,
                            max = rangeSetting.setting.end,
                            onValueChange = {
                                onSetSetting(SoundcoreSettingIds.MANUAL_NOISE_CANCELING, it.toValue())
                            },
                        )
                    }
                }

                "MultiScene" -> {
                    multiSceneAnc?.let { sceneSetting ->
                        Spacer(Modifier.height(12.dp))
                        SegmentedOptionRow(
                            options = sceneSetting.setting.options,
                            localizedOptions = sceneSetting.setting.localizedOptions,
                            selectedOption = sceneSetting.value,
                            onSelect = {
                                onSetSetting(SoundcoreSettingIds.MULTI_SCENE_NOISE_CANCELING, it.toValue())
                            },
                        )
                    }
                }

                "Adaptive" -> {
                    if (adaptiveNoiseCanceling != null) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoundcoreColors.SurfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.adaptive_anc_strength),
                                    color = SoundcoreColors.TextPrimary,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    text = adaptiveNoiseCanceling.translatedValue,
                                    color = SoundcoreColors.Cyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    if (realTimeAdaptiveAnc != null) {
                        Spacer(Modifier.height(10.dp))
                        InnerAmbientToggleRow(
                            label = stringResource(R.string.realtime_adaptive_anc),
                            checked = realTimeAdaptiveAnc.value,
                            onCheckedChange = {
                                onSetSetting(SoundcoreSettingIds.REAL_TIME_ADAPTIVE_ANC, it.toValue())
                            },
                        )
                    }
                    if (adaptiveAncSensitivity != null) {
                        Spacer(Modifier.height(10.dp))
                        SensitivitySliderBox(
                            value = adaptiveAncSensitivity.value,
                            min = adaptiveAncSensitivity.setting.start,
                            max = adaptiveAncSensitivity.setting.end,
                            onChange = {
                                onSetSetting(SoundcoreSettingIds.ADAPTIVE_ANC_SENSITIVITY, it.toValue())
                            },
                        )
                    }
                }
            }
        }

        if (currentMode == "Transparency" && transparencyMode != null) {
            Spacer(Modifier.height(16.dp))
            val selectedIndex = transparencyMode.setting.options.indexOf(transparencyMode.value)
            val currentLabel = transparencyMode.setting.localizedOptions.getOrElse(selectedIndex) {
                transparencyMode.value
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoundcoreColors.SurfaceElevated)
                    .clickable { showTransparencyModeDialog = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.mode),
                    color = SoundcoreColors.TextPrimary,
                    fontSize = 16.sp,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentLabel,
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 15.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.chevron_right_24px),
                        contentDescription = null,
                        tint = SoundcoreColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        if (windNoiseSuppression != null && currentMode != "Normal") {
            Spacer(Modifier.height(10.dp))
            val windDetected = windNoiseDetected?.value.equals("true", ignoreCase = true)
            InnerAmbientToggleRow(
                label = stringResource(R.string.wind_noise_reduction),
                subtitle = if (windDetected) stringResource(R.string.wind_noise_detected) else null,
                checked = windNoiseSuppression.value,
                onCheckedChange = {
                    onSetSetting(SoundcoreSettingIds.WIND_NOISE_SUPPRESSION, it.toValue())
                },
            )
        }
    }
}

@Composable
private fun InnerAmbientToggleRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoundcoreColors.SurfaceElevated)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = SoundcoreColors.TextPrimary,
                fontSize = 15.sp,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = SoundcoreColors.Cyan,
                    fontSize = 12.sp,
                )
            }
        }
        SoundcoreSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SegmentedOptionRow(
    options: List<String>,
    localizedOptions: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoundcoreColors.SurfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val label = localizedOptions.getOrElse(index) { option }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) SoundcoreColors.Cyan else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) SoundcoreColors.OnAccent else SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ManualAncLevelBar(
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoundcoreColors.SurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.anc_level),
                color = SoundcoreColors.TextPrimary,
                fontSize = 14.sp,
            )
            Text(
                text = "$value / $max",
                color = SoundcoreColors.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (level in min..max) {
                val active = level <= value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) SoundcoreColors.Cyan else SoundcoreColors.SurfacePill)
                        .clickable { onValueChange(level) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = level.toString(),
                        color = if (active) SoundcoreColors.OnAccent else SoundcoreColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SensitivitySliderBox(
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit,
) {
    val (displayedValue, setDisplayedValue) = throttledState(
        value = value,
        duration = 250,
        onValueChange = onChange,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoundcoreColors.SurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.adaptive_anc_sensitivity),
                color = SoundcoreColors.TextPrimary,
                fontSize = 14.sp,
            )
            Text(
                text = displayedValue.toString(),
                color = SoundcoreColors.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = displayedValue.toFloat(),
            onValueChange = { setDisplayedValue(it.roundToInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = SoundcoreColors.Cyan,
                activeTrackColor = SoundcoreColors.Cyan,
                inactiveTrackColor = SoundcoreColors.SurfacePill,
            ),
        )
    }
}

@Composable
fun OptionPickerDialog(
    title: String,
    options: List<String>,
    localizedOptions: List<String>,
    selectedOption: String?,
    onSelect: (String) -> Unit,
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
            Column {
                options.forEachIndexed { index, option ->
                    val label = localizedOptions.getOrElse(index) { option }
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) }
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
                    if (index < options.lastIndex) {
                        HorizontalDivider(color = SoundcoreColors.Divider, thickness = 0.5.dp)
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
