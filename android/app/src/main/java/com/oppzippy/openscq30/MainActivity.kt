package com.oppzippy.openscq30

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.oppzippy.openscq30.features.preferences.Preferences
import com.oppzippy.openscq30.lib.bindings.OpenScq30Session
import com.oppzippy.openscq30.ui.OpenSCQ30Root
import com.oppzippy.openscq30.ui.theme.ThemeType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        // HACK: On android versions <33, CompanionDeviceManager.Callback lacks onAssociationCreated, so we rely on
        // onActivityResult. The problem is, I'm not sure the proper way of passing data to the activity from
        // onDeviceFound, so we set this global callback as a workaround.
        var onPaired: (() -> Unit)? = null
    }

    @Inject
    lateinit var session: OpenScq30Session

    @Inject
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestHighestRefreshRate()

        lifecycleScope.launch {
            // Update status bar color when we change the theme via in app settings
            preferences.themeFlow.collectLatest { theme ->
                val detectDarkMode = { resources: Resources ->
                    when (theme) {
                        null -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                            Configuration.UI_MODE_NIGHT_YES

                        ThemeType.Light -> false

                        ThemeType.Dark -> true
                    }
                }
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        detectDarkMode,
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        detectDarkMode,
                    ),
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }
        setContent {
            OpenSCQ30Root()
        }
    }

    private fun requestHighestRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val currentDisplay = display ?: return
        val currentMode = currentDisplay.mode ?: return
        val bestMode = currentDisplay.supportedModes
            .filter {
                it.physicalWidth == currentMode.physicalWidth &&
                    it.physicalHeight == currentMode.physicalHeight
            }
            .maxByOrNull { it.refreshRate }
            ?: return
        if (window.attributes.preferredDisplayModeId != bestMode.modeId) {
            window.attributes = window.attributes.apply {
                preferredDisplayModeId = bestMode.modeId
            }
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.activity.ComponentActivity",
        ),
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // only used when CompanionDeviceManager.Callback::onAssociationCreated isn't supported
        if (requestCode == 0 && resultCode == RESULT_OK && data != null &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            lifecycleScope.launch {
                onPaired?.let { it() }
            }
        }
    }
}
