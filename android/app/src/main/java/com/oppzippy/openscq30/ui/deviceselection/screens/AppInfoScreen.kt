package com.oppzippy.openscq30.ui.deviceselection.screens

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun AppInfoScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(id = R.string.info),
            onBack = onBackClick,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SoundcoreCard {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyLarge.copy(color = SoundcoreColors.TextPrimary),
                ) {
                    HtmlText(stringResource(R.string.source_code))
                }
            }
        }
    }
}

@Composable
private fun HtmlText(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val customLinkifyTextView = remember { TextView(context) }

    val font = LocalTextStyle.current
    val color = LocalContentColor.current.convert(ColorSpaces.Srgb)
    val alpha = (color.alpha * 255).toInt()
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
        textView.textSize = font.fontSize.value
        textView.setTextColor((alpha shl 24) or (red shl 16) or (green shl 8) or blue)
        val html = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        textView.text = html
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAppInfoScreen() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        AppInfoScreen(onBackClick = {})
    }
}
