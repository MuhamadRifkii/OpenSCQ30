package com.oppzippy.openscq30.ui.soundcore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun SoundcoreTopBar(
    title: String? = null,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24px),
                contentDescription = stringResource(R.string.back),
                tint = SoundcoreColors.TextPrimary,
            )
        }
        if (title != null) {
            Text(
                text = title,
                color = SoundcoreColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        actions()
    }
}

@Composable
fun SoundcoreCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(SoundcoreColors.SurfaceCard)
    val clickableModifier = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(
        modifier = clickableModifier.padding(16.dp),
        content = content,
    )
}

@Composable
fun SoundcoreGroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SoundcoreColors.SurfaceCard),
        content = content,
    )
}

@Composable
fun SoundcoreNavRow(
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = SoundcoreColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
        if (!valueText.isNullOrBlank()) {
            Text(
                text = valueText,
                color = SoundcoreColors.TextSecondary,
                fontSize = 14.sp,
            )
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            painter = painterResource(R.drawable.chevron_right_24px),
            contentDescription = null,
            tint = SoundcoreColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SoundcoreSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = SoundcoreColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SoundcoreSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun SoundcoreSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SoundcoreColors.OnAccent,
            checkedTrackColor = SoundcoreColors.CyanBright,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = SoundcoreColors.TextSecondary,
            uncheckedTrackColor = SoundcoreColors.SurfacePill,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedThumbColor = SoundcoreColors.TextSecondary,
            disabledCheckedTrackColor = SoundcoreColors.SurfacePill,
            disabledUncheckedThumbColor = SoundcoreColors.TextTertiary,
            disabledUncheckedTrackColor = SoundcoreColors.SurfacePill,
        ),
    )
}

@Composable
fun SoundcoreDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = SoundcoreColors.Divider,
    )
}

@Composable
fun SoundcoreRadioIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) SoundcoreColors.Cyan else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SoundcoreColors.OnAccent),
            )
        } else {
            CanvasBorderCircle(color = SoundcoreColors.TextTertiary)
        }
    }
}

@Composable
private fun CanvasBorderCircle(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        drawCircle(
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx()),
        )
    }
}

@Composable
fun SoundcoreInfoRow(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = SoundcoreColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            color = SoundcoreColors.TextSecondary,
            fontSize = 14.sp,
        )
    }
}
