package com.oppzippy.openscq30.ui.soundcore.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.Equalizer
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreRadioIndicator
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

private val PresetPillShape = RoundedCornerShape(22.dp)
private val CustomEqCardShape = RoundedCornerShape(20.dp)
private val EqualizerGuideXFractions = floatArrayOf(0.08f, 0.36f, 0.64f, 0.92f)

@Composable
fun SoundEffectsScreen(
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
    onOpenCustomEq: () -> Unit,
) {
    val spatialAudio = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.SPATIAL_AUDIO)
    val spatialAudioMode = settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.SPATIAL_AUDIO_MODE)
    val surroundSound = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.SURROUND_SOUND)

    val presetEq = settings.getSetting<Setting.PresetEqualizerProfileSelect>(
        SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
    )
    val customEq = settings.getSetting<Setting.ModifiableSelectSetting>(
        SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
    )
    val volumeAdjustments = settings.getSetting<Setting.EqualizerSetting>(
        SoundcoreSettingIds.VOLUME_ADJUSTMENTS,
    )

    val selectedPresetIndex = presetEq?.value?.let { presetEq.select.options.indexOf(it) }?.takeIf { it >= 0 }
    val isPresetActive = selectedPresetIndex != null
    val isCustomActive = !isPresetActive && (customEq != null || volumeAdjustments != null)

    var lastCustomProfileName by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(customEq?.value) {
        val currentName = customEq?.value
        if (!currentName.isNullOrBlank()) {
            lastCustomProfileName = currentName
        }
    }

    val activateCustomEq: () -> Unit = {
        val savedProfiles = customEq?.setting?.options.orEmpty()
        val rememberedProfile = lastCustomProfileName?.takeIf { it in savedProfiles }
        val targetProfile = customEq?.value ?: rememberedProfile
        when {
            targetProfile != null -> onSetSetting(
                SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                targetProfile.toValue(),
            )
            presetEq != null -> onSetSetting(
                SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
                (null as String?).toValue(),
            )
            savedProfiles.isNotEmpty() -> onSetSetting(
                SoundcoreSettingIds.CUSTOM_EQUALIZER_PROFILE,
                savedProfiles.first().toValue(),
            )
        }
    }

    var showAllPresetsDialog by remember { mutableStateOf(false) }

    if (showAllPresetsDialog && presetEq != null) {
        AllPresetsDialog(
            options = presetEq.select.options,
            localizedOptions = presetEq.select.localizedOptions,
            selectedIndex = selectedPresetIndex,
            onSelect = { idx ->
                onSetSetting(
                    SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
                    presetEq.select.options[idx].toValue(),
                )
                showAllPresetsDialog = false
            },
            onDismiss = { showAllPresetsDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(R.string.sound_effects),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (spatialAudio != null) {
                SoundcoreCard(
                    onClick = { onSetSetting(SoundcoreSettingIds.SPATIAL_AUDIO, (!spatialAudio.value).toValue()) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.speaker_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.spatial_audio),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        SoundcoreRadioIndicator(selected = spatialAudio.value)
                    }
                    if (spatialAudio.value && spatialAudioMode != null) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoundcoreColors.SurfaceElevated)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            spatialAudioMode.setting.options.forEachIndexed { idx, opt ->
                                val selected = opt == spatialAudioMode.value
                                val label = spatialAudioMode.setting.localizedOptions.getOrElse(idx) { opt }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) SoundcoreColors.Cyan else Color.Transparent)
                                        .clickable {
                                            onSetSetting(SoundcoreSettingIds.SPATIAL_AUDIO_MODE, opt.toValue())
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) {
                                            SoundcoreColors.OnAccent
                                        } else {
                                            SoundcoreColors.TextSecondary
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (surroundSound != null) {
                SoundcoreCard(
                    onClick = { onSetSetting(SoundcoreSettingIds.SURROUND_SOUND, (!surroundSound.value).toValue()) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.speaker_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.surround_sound),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        SoundcoreRadioIndicator(selected = surroundSound.value)
                    }
                }
            }

            if (presetEq != null) {
                val previewValues = remember(selectedPresetIndex, presetEq, volumeAdjustments) {
                    when {
                        selectedPresetIndex != null -> presetEq.presets.getOrNull(selectedPresetIndex)
                        volumeAdjustments != null -> volumeAdjustments.value
                        else -> null
                    } ?: List(presetEq.equalizer.bandHz.size) { 0.toShort() }
                }

                SoundcoreCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.equalizer_24px),
                            contentDescription = null,
                            tint = SoundcoreColors.TextPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.default_presets),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    GradientEqualizerPreview(
                        equalizer = presetEq.equalizer,
                        values = previewValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                    )

                    Spacer(Modifier.height(16.dp))

                    val quickIndices = remember(presetEq.select.options.size, selectedPresetIndex) {
                        buildQuickPresetIndices(
                            total = presetEq.select.options.size,
                            selectedIndex = selectedPresetIndex,
                        )
                    }

                    // 2-column grid of up to 5 presets + "More >" pill
                    val rows = remember(quickIndices) { (quickIndices + listOf(null)).chunked(2) }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowItems.forEach { presetIndex ->
                                    if (presetIndex != null) {
                                        val isSelected = presetIndex == selectedPresetIndex
                                        val title = presetEq.select.localizedOptions.getOrElse(presetIndex) {
                                            presetEq.select.options[presetIndex]
                                        }
                                        PresetPill(
                                            text = title,
                                            isSelected = isSelected,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                onSetSetting(
                                                    SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE,
                                                    presetEq.select.options[presetIndex].toValue(),
                                                )
                                            },
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(PresetPillShape)
                                                .background(SoundcoreColors.SurfacePill)
                                                .clickable { showAllPresetsDialog = true },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = stringResource(R.string.more),
                                                    color = SoundcoreColors.TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                                Spacer(Modifier.width(2.dp))
                                                Icon(
                                                    painter = painterResource(R.drawable.chevron_right_24px),
                                                    contentDescription = null,
                                                    tint = SoundcoreColors.TextSecondary,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (customEq != null || volumeAdjustments != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CustomEqCardShape)
                        .background(SoundcoreColors.SurfaceCard),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenCustomEq)
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.custom_eq),
                                    color = SoundcoreColors.TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.chevron_right_24px),
                                    contentDescription = null,
                                    tint = SoundcoreColors.TextSecondary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = customEq?.value
                                    ?: lastCustomProfileName?.takeIf { it in customEq?.setting?.options.orEmpty() }
                                    ?: stringResource(R.string.custom),
                                color = SoundcoreColors.TextSecondary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    VerticalDivider(
                        modifier = Modifier.height(34.dp),
                        thickness = 0.5.dp,
                        color = SoundcoreColors.Divider,
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 68.dp)
                            .clickable(
                                role = Role.RadioButton,
                                onClick = activateCustomEq,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        SoundcoreRadioIndicator(selected = isCustomActive)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun buildQuickPresetIndices(total: Int, selectedIndex: Int?): List<Int> {
    if (total <= 5) return (0 until total).toList()
    val base = mutableListOf(0, 1, 2, 3, 4)
    if (selectedIndex != null && selectedIndex !in base && selectedIndex in 0 until total) {
        base[4] = selectedIndex
    }
    return base
}

@Composable
private fun PresetPill(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val gradientPink = SoundcoreColors.GradientPink
    val gradientCyan = SoundcoreColors.GradientCyan
    val pillColor = SoundcoreColors.SurfacePill
    val selectedBrush = remember(gradientPink, gradientCyan) {
        Brush.horizontalGradient(
            colors = listOf(gradientPink, gradientCyan),
        )
    }
    val backgroundModifier = if (isSelected) {
        Modifier.background(brush = selectedBrush)
    } else {
        Modifier.background(pillColor)
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(PresetPillShape)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isSelected) SoundcoreColors.OnAccent else SoundcoreColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun GradientEqualizerPreview(
    equalizer: Equalizer,
    values: List<Short>,
    modifier: Modifier = Modifier,
) {
    val guideColor = SoundcoreColors.Divider
    val gradientPink = SoundcoreColors.GradientPink
    val gradientCyan = SoundcoreColors.GradientCyan
    val lineBrush = remember(gradientPink, gradientCyan) {
        Brush.horizontalGradient(
            colors = listOf(gradientPink, gradientCyan),
        )
    }
    val fillBrush = remember(gradientPink) {
        Brush.verticalGradient(
            colors = listOf(
                gradientPink.copy(alpha = 0.22f),
                Color.Transparent,
            ),
        )
    }
    val linePath = remember { Path() }
    val fillPath = remember { Path() }

    Canvas(modifier = modifier) {
        val visibleCount = equalizer.bandHz.size.coerceAtMost(values.size)
        if (visibleCount <= 0) return@Canvas
        val w = size.width
        val h = size.height
        val padY = 8.dp.toPx()
        val usableH = (h - padY * 2f).coerceAtLeast(1f)
        val range = (equalizer.max - equalizer.min).toFloat().coerceAtLeast(1f)
        val guideStrokeWidth = 1.dp.toPx()

        // Subtle vertical guide lines (like Soundcore EQ card)
        for (xFrac in EqualizerGuideXFractions) {
            drawLine(
                color = guideColor,
                start = Offset(w * xFrac, 0f),
                end = Offset(w * xFrac, h),
                strokeWidth = guideStrokeWidth,
            )
        }
        // Subtle 0 dB center line
        val zeroY = padY + usableH * (1f - (0f - equalizer.min) / range)
        drawLine(
            color = guideColor,
            start = Offset(0f, zeroY),
            end = Offset(w, zeroY),
            strokeWidth = guideStrokeWidth,
        )

        linePath.reset()
        fillPath.reset()

        val minVal = equalizer.min
        val firstX = equalizerPointX(0, visibleCount, w)
        val firstY = equalizerPointY(values[0], minVal, range, padY, usableH)
        var prevX = firstX
        var prevY = firstY
        linePath.moveTo(firstX, firstY)
        for (i in 1 until visibleCount) {
            val currX = equalizerPointX(i, visibleCount, w)
            val currY = equalizerPointY(values[i], minVal, range, padY, usableH)
            val midX = (prevX + currX) / 2f
            linePath.cubicTo(midX, prevY, midX, currY, currX, currY)
            prevX = currX
            prevY = currY
        }

        fillPath.addPath(linePath)
        fillPath.lineTo(prevX, h)
        fillPath.lineTo(firstX, h)
        fillPath.close()

        drawPath(path = fillPath, brush = fillBrush)
        drawPath(
            path = linePath,
            brush = lineBrush,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

private fun equalizerPointX(index: Int, count: Int, width: Float): Float =
    if (count > 1) width * (index.toFloat() / (count - 1)) else width / 2f

private fun equalizerPointY(
    value: Short,
    minValue: Short,
    range: Float,
    padY: Float,
    usableHeight: Float,
): Float {
    val normalized = ((value - minValue).toFloat() / range).coerceIn(0f, 1f)
    return padY + usableHeight * (1f - normalized)
}

@Composable
private fun AllPresetsDialog(
    options: List<String>,
    localizedOptions: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = SoundcoreColors.SurfaceCard,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.all_presets),
                color = SoundcoreColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = options,
                    key = { index, option -> "$index:$option" },
                ) { index, option ->
                    val label = localizedOptions.getOrElse(index) { option }
                    PresetPill(
                        text = label,
                        isSelected = index == selectedIndex,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSelect(index) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = SoundcoreColors.TextSecondary)
            }
        },
    )
}
