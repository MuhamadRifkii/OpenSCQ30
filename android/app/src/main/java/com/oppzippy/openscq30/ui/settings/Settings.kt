package com.oppzippy.openscq30.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreGroupedCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreNavRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitchRow
import com.oppzippy.openscq30.ui.soundcore.screens.OptionPickerDialog
import com.oppzippy.openscq30.ui.theme.BackTransitionDirection
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.theme.ThemeType

@Composable
fun Settings(viewModel: SettingsViewModel = hiltViewModel()) {
    val autoConnect by viewModel.autoConnect.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val amoledBlack by viewModel.amoledBlack.collectAsState()
    val predictiveBackEnabled by viewModel.predictiveBackEnabled.collectAsState()
    val slideTransitionEnabled by viewModel.slideTransitionEnabled.collectAsState()
    val backTransitionDirection by viewModel.backTransitionDirection.collectAsState()
    Settings(
        autoConnect = autoConnect,
        onAutoConnectChange = { viewModel.setAutoConnect(it) },
        theme = theme,
        onThemeChange = { viewModel.setTheme(it) },
        dynamicColorEnabled = dynamicColorEnabled,
        onDynamicColorChange = { viewModel.setDynamicColor(it) },
        amoledBlack = amoledBlack,
        onAmoledBlackChange = { viewModel.setAmoledBlack(it) },
        predictiveBackEnabled = predictiveBackEnabled,
        onPredictiveBackEnabledChange = { viewModel.setPredictiveBackEnabled(it) },
        slideTransitionEnabled = slideTransitionEnabled,
        onSlideTransitionEnabledChange = { viewModel.setSlideTransitionEnabled(it) },
        backTransitionDirection = backTransitionDirection,
        onBackTransitionDirectionChange = { viewModel.setBackTransitionDirection(it) },
        onCopyLogs = { viewModel.copyLogs() },
        onCopyLogsUnfiltered = { viewModel.copyLogsUnfiltered() },
    )
}

@Composable
private fun Settings(
    autoConnect: Boolean,
    onAutoConnectChange: (Boolean) -> Unit,
    theme: ThemeType?,
    onThemeChange: (ThemeType?) -> Unit,
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    amoledBlack: Boolean,
    onAmoledBlackChange: (Boolean) -> Unit,
    predictiveBackEnabled: Boolean,
    onPredictiveBackEnabledChange: (Boolean) -> Unit,
    slideTransitionEnabled: Boolean,
    onSlideTransitionEnabledChange: (Boolean) -> Unit,
    backTransitionDirection: BackTransitionDirection,
    onBackTransitionDirectionChange: (BackTransitionDirection) -> Unit,
    onCopyLogs: () -> Unit,
    onCopyLogsUnfiltered: () -> Unit,
) {
    val themes = listOf(
        null to stringResource(R.string.system_theme),
        ThemeType.Light to stringResource(R.string.light),
        ThemeType.Dark to stringResource(R.string.dark),
    )
    val directions = listOf(
        BackTransitionDirection.LeftToRightOpen to stringResource(R.string.direction_open_ltr),
        BackTransitionDirection.RightToLeftOpen to stringResource(R.string.direction_open_rtl),
        BackTransitionDirection.FollowSwipeEdge to stringResource(R.string.direction_follow_swipe_edge),
    )

    var showThemeDialog by remember { mutableStateOf(false) }
    var showDirectionDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        val options = themes.map { it.first?.name ?: "SYSTEM" }
        val labels = themes.map { it.second }
        OptionPickerDialog(
            title = stringResource(R.string.theme),
            options = options,
            localizedOptions = labels,
            selectedOption = theme?.name ?: "SYSTEM",
            onSelect = { selected ->
                onThemeChange(themes.find { (t, _) -> (t?.name ?: "SYSTEM") == selected }?.first)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showDirectionDialog) {
        OptionPickerDialog(
            title = stringResource(R.string.back_transition_direction),
            options = directions.map { it.first.name },
            localizedOptions = directions.map { it.second },
            selectedOption = backTransitionDirection.name,
            onSelect = { selected ->
                directions.find { it.first.name == selected }?.first?.let(onBackTransitionDirectionChange)
                showDirectionDialog = false
            },
            onDismiss = { showDirectionDialog = false },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SoundcoreGroupedCard {
            SoundcoreSwitchRow(
                title = stringResource(R.string.auto_connect),
                checked = autoConnect,
                onCheckedChange = onAutoConnectChange,
            )
        }

        SoundcoreGroupedCard {
            val currentThemeLabel = themes.find { it.first == theme }?.second ?: ""
            SoundcoreNavRow(
                title = stringResource(R.string.theme),
                valueText = currentThemeLabel,
                onClick = { showThemeDialog = true },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SoundcoreDivider()
                SoundcoreSwitchRow(
                    title = stringResource(R.string.dynamic_color),
                    checked = dynamicColorEnabled,
                    onCheckedChange = onDynamicColorChange,
                )
            }
            if (theme != ThemeType.Light) {
                SoundcoreDivider()
                SoundcoreSwitchRow(
                    title = stringResource(R.string.amoled_black),
                    checked = amoledBlack,
                    onCheckedChange = onAmoledBlackChange,
                )
            }
        }

        SoundcoreGroupedCard {
            SoundcoreSwitchRow(
                title = stringResource(R.string.predictive_back),
                checked = predictiveBackEnabled,
                onCheckedChange = onPredictiveBackEnabledChange,
            )
            SoundcoreDivider()
            SoundcoreSwitchRow(
                title = stringResource(R.string.slide_back_transitions),
                checked = slideTransitionEnabled,
                onCheckedChange = onSlideTransitionEnabledChange,
            )
            if (slideTransitionEnabled || predictiveBackEnabled) {
                SoundcoreDivider()
                val currentDirLabel = directions.find { it.first == backTransitionDirection }?.second ?: ""
                SoundcoreNavRow(
                    title = stringResource(R.string.back_transition_direction),
                    subtitle = currentDirLabel,
                    onClick = { showDirectionDialog = true },
                )
            }
        }

        SoundcoreGroupedCard {
            SoundcoreNavRow(
                title = stringResource(R.string.copy_logs_to_clipboard),
                onClick = onCopyLogs,
            )
            SoundcoreDivider()
            SoundcoreNavRow(
                title = stringResource(R.string.copy_logs_to_clipboard_unfiltered),
                onClick = onCopyLogsUnfiltered,
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        Settings(
            autoConnect = false,
            onAutoConnectChange = {},
            theme = null,
            onThemeChange = {},
            dynamicColorEnabled = true,
            onDynamicColorChange = {},
            amoledBlack = false,
            onAmoledBlackChange = {},
            predictiveBackEnabled = true,
            onPredictiveBackEnabledChange = {},
            slideTransitionEnabled = true,
            onSlideTransitionEnabledChange = {},
            backTransitionDirection = BackTransitionDirection.LeftToRightOpen,
            onBackTransitionDirectionChange = {},
            onCopyLogsUnfiltered = {},
            onCopyLogs = {},
        )
    }
}
