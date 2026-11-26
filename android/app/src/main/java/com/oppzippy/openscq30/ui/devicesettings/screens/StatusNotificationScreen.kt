package com.oppzippy.openscq30.ui.devicesettings.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.features.soundcoredevice.service.DeviceService
import com.oppzippy.openscq30.lib.bindings.translateSettingId
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreNavRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.utils.PermissionCheck

@Composable
fun StatusNotificationScreen(
    settingIds: List<String>,
    featuredSettingSlots: List<String?>,
    onFeaturedSettingSlotChange: (Int, String?) -> Unit,
    quickPresets: List<String>,
    quickPresetSlots: List<String?>,
    onQuickPresetSlotChange: (Int, String?) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        if (onBack != null) {
            SoundcoreTopBar(
                title = stringResource(R.string.status_notification),
                onBack = onBack,
            )
        }
        val inner = @Composable {
            StatusNotificationInner(
                settingIds = settingIds,
                featuredSettingSlots = featuredSettingSlots,
                onFeaturedSettingSlotChange = onFeaturedSettingSlotChange,
                quickPresets = quickPresets,
                quickPresetSlots = quickPresetSlots,
                onQuickPresetSlotChange = onQuickPresetSlotChange,
            )
        }

        // Older android versions don't require any special permission for foreground service notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCheck(
                prompt = stringResource(R.string.notification_permission_is_required),
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionGranted = {
                    // Since we may have not had notification permission before this point, we need to resend the
                    // notification to ensure it is visible.
                    context.sendBroadcast(
                        Intent().apply {
                            action = DeviceService.ACTION_SEND_NOTIFICATION
                            `package` = context.packageName
                        },
                    )
                },
            ) {
                inner()
            }
        } else {
            inner()
        }
    }
}

private sealed class NotificationPickerTarget {
    data class FeaturedSetting(val slotIndex: Int) : NotificationPickerTarget()
    data class QuickPreset(val slotIndex: Int) : NotificationPickerTarget()
}

@Composable
private fun StatusNotificationInner(
    settingIds: List<String>,
    featuredSettingSlots: List<String?>,
    onFeaturedSettingSlotChange: (Int, String?) -> Unit,
    quickPresets: List<String>,
    quickPresetSlots: List<String?>,
    onQuickPresetSlotChange: (Int, String?) -> Unit,
) {
    var activePicker by remember { mutableStateOf<NotificationPickerTarget?>(null) }

    activePicker?.let { target ->
        when (target) {
            is NotificationPickerTarget.FeaturedSetting -> {
                val currentValue = featuredSettingSlots.getOrNull(target.slotIndex)
                OptionalStringPickerDialog(
                    title = stringResource(R.string.featured_setting_x, target.slotIndex + 1),
                    options = settingIds,
                    localizedOptions = settingIds.map { translateSettingId(it) },
                    selectedOption = currentValue,
                    onSelect = {
                        onFeaturedSettingSlotChange(target.slotIndex, it)
                        activePicker = null
                    },
                    onDismiss = { activePicker = null },
                )
            }

            is NotificationPickerTarget.QuickPreset -> {
                val currentValue = quickPresetSlots.getOrNull(target.slotIndex)
                OptionalStringPickerDialog(
                    title = stringResource(R.string.quick_preset_slot_x, target.slotIndex + 1),
                    options = quickPresets,
                    localizedOptions = quickPresets,
                    selectedOption = currentValue,
                    onSelect = {
                        onQuickPresetSlotChange(target.slotIndex, it)
                        activePicker = null
                    },
                    onDismiss = { activePicker = null },
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SoundcoreGroupedCard {
            featuredSettingSlots.forEachIndexed { index, settingId ->
                SoundcoreNavRow(
                    title = stringResource(R.string.featured_setting_x, index + 1),
                    valueText = settingId?.let { translateSettingId(it) } ?: stringResource(R.string.none),
                    onClick = { activePicker = NotificationPickerTarget.FeaturedSetting(index) },
                )
                if (index < featuredSettingSlots.lastIndex) {
                    SoundcoreDivider()
                }
            }
        }

        SoundcoreGroupedCard {
            quickPresetSlots.forEachIndexed { index, slot ->
                SoundcoreNavRow(
                    title = stringResource(R.string.quick_preset_slot_x, index + 1),
                    valueText = slot ?: stringResource(R.string.none),
                    onClick = { activePicker = NotificationPickerTarget.QuickPreset(index) },
                )
                if (index < quickPresetSlots.lastIndex) {
                    SoundcoreDivider()
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OptionalStringPickerDialog(
    title: String,
    options: List<String>,
    localizedOptions: List<String>,
    selectedOption: String?,
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
                val noneSelected = selectedOption == null
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
                        color = if (noneSelected) SoundcoreColors.Cyan else SoundcoreColors.TextPrimary,
                        fontSize = 15.sp,
                    )
                    if (noneSelected) {
                        Icon(
                            painter = painterResource(R.drawable.check_24px),
                            contentDescription = stringResource(R.string.selected),
                            tint = SoundcoreColors.Cyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                HorizontalDivider(color = SoundcoreColors.Divider, thickness = 0.5.dp)

                options.forEachIndexed { idx, opt ->
                    val isSelected = opt == selectedOption
                    val label = localizedOptions.getOrElse(idx) { opt }
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
                    if (idx < options.lastIndex) {
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
