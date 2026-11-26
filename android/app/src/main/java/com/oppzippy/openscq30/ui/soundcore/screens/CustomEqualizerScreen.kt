package com.oppzippy.openscq30.ui.soundcore.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.devicesettings.composables.Equalizer
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreRadioIndicator
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.utils.ModifiableSelect

@Composable
fun CustomEqualizerScreen(
    settings: Map<String, Setting>,
    hasImportExport: Boolean,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
    onOpenImportExport: () -> Unit,
) {
    val presetProfile = settings.getSetting<Setting.PresetEqualizerProfileSelect>(
        SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
    )
    val customProfile = settings.getSetting<Setting.ModifiableSelectSetting>(
        SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
    )
    val volumeAdjustments = settings.getSetting<Setting.EqualizerSetting>(
        SoundcoreSettingIds.VOLUME_ADJUSTMENTS,
    )
    val isCustomActive = presetProfile?.value == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(R.string.custom_eq),
            onBack = onBack,
            actions = {
                if (hasImportExport) {
                    IconButton(onClick = onOpenImportExport) {
                        Icon(
                            painter = painterResource(R.drawable.swap_vert_24px),
                            contentDescription = stringResource(R.string.import_export_eq),
                            tint = SoundcoreColors.TextPrimary,
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (presetProfile != null) {
                SoundcoreCard(
                    onClick = if (!isCustomActive) {
                        {
                            val targetName = customProfile?.value
                                ?: customProfile?.setting?.options?.firstOrNull()
                            if (targetName != null) {
                                onSetSetting(
                                    SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                                    targetName.toValue(),
                                )
                            } else {
                                onSetSetting(
                                    SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
                                    (null as String?).toValue(),
                                )
                            }
                        }
                    } else {
                        null
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (isCustomActive) {
                                stringResource(R.string.custom_eq_active)
                            } else {
                                stringResource(R.string.activate_custom_eq)
                            },
                            color = if (isCustomActive) SoundcoreColors.TextPrimary else SoundcoreColors.Cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        SoundcoreRadioIndicator(selected = isCustomActive)
                    }
                }
            }

            if (volumeAdjustments != null) {
                SoundcoreCard {
                    GradientEqualizerPreview(
                        equalizer = volumeAdjustments.setting,
                        values = volumeAdjustments.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                    )
                }
            }

            if (customProfile != null) {
                SoundcoreCard {
                    Text(
                        text = stringResource(R.string.custom_profile),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(8.dp))
                    ModifiableSelect(
                        name = stringResource(R.string.custom_profile),
                        showLabel = false,
                        options = customProfile.setting.options,
                        selectedIndex = customProfile.setting.options.indexOf(customProfile.value)
                            .let { if (it == -1) null else it },
                        onSelect = { idx ->
                            onSetSetting(
                                SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                                customProfile.setting.options[idx].toValue(),
                            )
                        },
                        onAddOption = { name ->
                            onSetSetting(
                                SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                                ModifiableSelectCommandInner.Add(name).toValue(),
                            )
                        },
                        onRemoveOption = { idx ->
                            onSetSetting(
                                SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                                ModifiableSelectCommandInner.Remove(customProfile.setting.options[idx]).toValue(),
                            )
                        },
                    )
                }
            }

            if (volumeAdjustments != null) {
                SoundcoreCard {
                    Equalizer(
                        bands = volumeAdjustments.setting.bandHz,
                        values = volumeAdjustments.value,
                        minValue = volumeAdjustments.setting.min,
                        maxValue = volumeAdjustments.setting.max,
                        fractionDigits = volumeAdjustments.setting.fractionDigits,
                        onValueChange = { changedIndex, newValue ->
                            val updated = volumeAdjustments.value.mapIndexed { idx, old ->
                                if (idx == changedIndex) newValue else old
                            }
                            onSetSetting(SoundcoreSettingIds.VOLUME_ADJUSTMENTS, updated.toValue())
                        },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
