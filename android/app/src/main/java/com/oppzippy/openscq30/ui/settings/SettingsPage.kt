package com.oppzippy.openscq30.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun SettingsPage(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(id = R.string.settings),
            onBack = onBackClick,
        )
        Settings()
    }
}
