package com.oppzippy.openscq30.ui.devicesettings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.devicesettings.Screen
import com.oppzippy.openscq30.ui.devicesettings.ScreenInfo
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreNavRow
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

private val SingleCategoryCardShape = RoundedCornerShape(20.dp)
private val TopCategoryCardShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val BottomCategoryCardShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
fun CategorySelectionScreen(
    screens: List<ScreenInfo>,
    onNavigation: (Screen) -> Unit,
    title: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        if (onBack != null) {
            SoundcoreTopBar(
                title = title ?: stringResource(R.string.all_settings_classic),
                onBack = onBack,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
        ) {
            itemsIndexed(
                items = screens,
                key = { _, screenInfo -> categoryScreenKey(screenInfo.baseRoute) },
            ) { idx, screenInfo ->
                val shape = when {
                    screens.size == 1 -> SingleCategoryCardShape
                    idx == 0 -> TopCategoryCardShape
                    idx == screens.lastIndex -> BottomCategoryCardShape
                    else -> RectangleShape
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(SoundcoreColors.SurfaceCard),
                ) {
                    SoundcoreNavRow(
                        title = screenInfo.name.translated(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(screenInfo.icon),
                                contentDescription = null,
                                tint = SoundcoreColors.TextPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        onClick = { onNavigation(screenInfo.baseRoute) },
                    )
                    if (idx < screens.lastIndex) {
                        SoundcoreDivider()
                    }
                }
            }
        }
    }
}

private fun categoryScreenKey(route: Screen): String = when (route) {
    is Screen.SettingsCategory -> "category:${route.categoryId}"
    else -> route.toString()
}

@Preview
@Composable
private fun PreviewCategorySelectionScreen() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        CategorySelectionScreen(
            screens = listOf(
                Screen.SettingsCategory("general").screenInfo(),
                Screen.SettingsCategory("equalizer").screenInfo(),
                Screen.SettingsCategory("buttonConfiguration").screenInfo(),
                Screen.QuickPresets.screenInfo,
            ),
            onNavigation = {},
            onBack = {},
        )
    }
}
