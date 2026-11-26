package com.oppzippy.openscq30.ui.soundcore.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreInfoRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreNavRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitchRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun MoreSettingsScreen(
    deviceModel: String,
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
    onOpenSafeVolume: () -> Unit,
    onOpenDualConnections: () -> Unit,
    onOpenStatusNotification: () -> Unit,
    onOpenQuickPresets: () -> Unit,
    onOpenClassicView: () -> Unit,
    onOpenAppSettings: () -> Unit = {},
    onDisconnect: () -> Unit = {},
) {
    val context = LocalContext.current
    val shortModelName = translateDeviceModel(deviceModel).removePrefix("Soundcore ").ifBlank {
        translateDeviceModel(deviceModel)
    }

    val hasSafeVolume = settings.containsKey(SoundcoreSettingIds.LIMIT_HIGH_VOLUME)
    val hasDualConnections = settings.containsKey(SoundcoreSettingIds.DUAL_CONNECTIONS)

    val ldac = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.LDAC)
    val gamingMode = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.GAMING_MODE)
    val touchTone = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.TOUCH_TONE)
    val lowBatteryPrompt = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.LOW_BATTERY_PROMPT)
    val autoPowerOff = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.AUTO_POWER_OFF)

    val fwLeft = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.FIRMWARE_VERSION_LEFT)
    val fwRight = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.FIRMWARE_VERSION_RIGHT)
    val serialNumber = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.SERIAL_NUMBER)
    val twsStatus = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.TWS_STATUS)

    var showAutoPowerOffDialog by remember { mutableStateOf(false) }

    if (showAutoPowerOffDialog && autoPowerOff != null) {
        OptionPickerDialog(
            title = stringResource(R.string.auto_power_off),
            options = autoPowerOff.setting.options,
            localizedOptions = autoPowerOff.setting.localizedOptions,
            selectedOption = autoPowerOff.value,
            onSelect = {
                onSetSetting(SoundcoreSettingIds.AUTO_POWER_OFF, it.toValue())
                showAutoPowerOffDialog = false
            },
            onDismiss = { showAutoPowerOffDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(R.string.more_settings),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (hasSafeVolume) {
                SoundcoreGroupedCard {
                    SoundcoreNavRow(
                        title = stringResource(R.string.safe_volume),
                        onClick = onOpenSafeVolume,
                    )
                }
            }

            if (hasDualConnections) {
                SoundcoreGroupedCard {
                    SoundcoreNavRow(
                        title = stringResource(R.string.dual_device_connection),
                        onClick = onOpenDualConnections,
                    )
                }
            }

            val toggleEntries = buildList {
                ldac?.let { add(Triple(SoundcoreSettingIds.LDAC, R.string.ldac, it.value)) }
                gamingMode?.let { add(Triple(SoundcoreSettingIds.GAMING_MODE, R.string.gaming_mode, it.value)) }
                touchTone?.let { add(Triple(SoundcoreSettingIds.TOUCH_TONE, R.string.prompt_tone, it.value)) }
                lowBatteryPrompt?.let {
                    add(Triple(SoundcoreSettingIds.LOW_BATTERY_PROMPT, R.string.low_battery_prompt, it.value))
                }
            }
            if (toggleEntries.isNotEmpty()) {
                SoundcoreGroupedCard {
                    toggleEntries.forEachIndexed { idx, (settingId, titleRes, value) ->
                        SoundcoreSwitchRow(
                            title = stringResource(titleRes),
                            checked = value,
                            onCheckedChange = { onSetSetting(settingId, it.toValue()) },
                        )
                        if (idx < toggleEntries.lastIndex) {
                            SoundcoreDivider()
                        }
                    }
                }
            }

            SoundcoreGroupedCard {
                if (autoPowerOff != null) {
                    val idx = autoPowerOff.setting.options.indexOf(autoPowerOff.value)
                    val valueLabel = autoPowerOff.setting.localizedOptions.getOrElse(idx) { autoPowerOff.value }
                    SoundcoreNavRow(
                        title = stringResource(R.string.auto_power_off),
                        valueText = valueLabel,
                        onClick = { showAutoPowerOffDialog = true },
                    )
                    SoundcoreDivider()
                }
                SoundcoreNavRow(
                    title = stringResource(R.string.status_notification),
                    onClick = onOpenStatusNotification,
                )
                SoundcoreDivider()
                SoundcoreNavRow(
                    title = stringResource(R.string.quick_presets),
                    onClick = onOpenQuickPresets,
                )
                SoundcoreDivider()
                SoundcoreNavRow(
                    title = stringResource(R.string.all_settings_classic),
                    onClick = onOpenClassicView,
                )
                SoundcoreDivider()
                SoundcoreNavRow(
                    title = stringResource(R.string.app_settings),
                    onClick = onOpenAppSettings,
                )
            }

            SoundcoreGroupedCard {
                SoundcoreInfoRow(
                    title = stringResource(R.string.device_name),
                    value = shortModelName,
                )
                if (fwLeft != null || fwRight != null) {
                    SoundcoreDivider()
                    val fwText = when {
                        fwLeft != null && fwRight != null && fwLeft.translatedValue == fwRight.translatedValue ->
                            fwLeft.translatedValue

                        fwLeft != null && fwRight != null ->
                            "L: ${fwLeft.translatedValue}  R: ${fwRight.translatedValue}"

                        else -> (fwLeft ?: fwRight)!!.translatedValue
                    }
                    SoundcoreInfoRow(
                        title = stringResource(R.string.firmware_version),
                        value = fwText,
                    )
                }
                if (serialNumber != null) {
                    SoundcoreDivider()
                    SoundcoreInfoRow(
                        title = stringResource(R.string.serial_number),
                        value = serialNumber.translatedValue,
                        onClick = {
                            val cm = context.getSystemService<ClipboardManager>()
                            cm?.setPrimaryClip(ClipData.newPlainText("Serial Number", serialNumber.translatedValue))
                            Toast.makeText(
                                context,
                                context.getString(R.string.copied_serial_number),
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    )
                }
                if (twsStatus != null) {
                    SoundcoreDivider()
                    SoundcoreInfoRow(
                        title = stringResource(R.string.tws_status),
                        value = twsStatus.translatedValue,
                    )
                }
            }

            SoundcoreCard(onClick = onDisconnect) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.disconnect),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
