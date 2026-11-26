package com.oppzippy.openscq30.features.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.oppzippy.openscq30.ui.theme.BackTransitionDirection
import com.oppzippy.openscq30.ui.theme.ThemeType
import com.oppzippy.openscq30.ui.theme.prefersDarkTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class Preferences @Inject constructor(@ApplicationContext context: Context) {
    companion object {
        const val TAG = "Preferences"

        private const val PREFERENCE_AUTO_CONNECT = "autoConnect"
        private const val PREFERENCE_THEME = "theme"
        private const val PREFERENCE_DYNAMIC_COLOR = "dynamicColor"
        private const val PREFERENCE_AMOLED_BLACK = "amoledBlack"
        private const val PREFERENCE_PREDICTIVE_BACK_ENABLED = "predictiveBackEnabled"
        private const val PREFERENCE_SLIDE_TRANSITION_ENABLED = "slideTransitionEnabled"
        private const val PREFERENCE_BACK_TRANSITION_DIRECTION = "backTransitionDirection"
    }

    private val preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    private val autoConnectPreference = Preference(
        get = { preferences.getBoolean(PREFERENCE_AUTO_CONNECT, false) },
        set = { preferences.edit { putBoolean(PREFERENCE_AUTO_CONNECT, it) } },
    )
    private val themePreference = Preference(
        get = {
            preferences.getString(PREFERENCE_THEME, null)?.let { themeName ->
                try {
                    ThemeType.valueOf(themeName)
                } catch (ex: IllegalArgumentException) {
                    Log.e(TAG, "error parsing theme: $themeName", ex)
                    null
                }
            }
        },
        set = {
            preferences.edit {
                if (it != null) {
                    putString(PREFERENCE_THEME, it.name)
                } else {
                    remove(PREFERENCE_THEME)
                }
            }
        },
    )
    private val dynamicColorPreference = Preference(
        get = { preferences.getBoolean(PREFERENCE_DYNAMIC_COLOR, true) },
        set = { preferences.edit { putBoolean(PREFERENCE_DYNAMIC_COLOR, it) } },
    )
    private val amoledBlackPreference = Preference(
        get = { preferences.getBoolean(PREFERENCE_AMOLED_BLACK, false) },
        set = { preferences.edit { putBoolean(PREFERENCE_AMOLED_BLACK, it) } },
    )
    private val predictiveBackEnabledPreference = Preference(
        get = { preferences.getBoolean(PREFERENCE_PREDICTIVE_BACK_ENABLED, true) },
        set = { preferences.edit { putBoolean(PREFERENCE_PREDICTIVE_BACK_ENABLED, it) } },
    )
    private val slideTransitionEnabledPreference = Preference(
        get = { preferences.getBoolean(PREFERENCE_SLIDE_TRANSITION_ENABLED, true) },
        set = { preferences.edit { putBoolean(PREFERENCE_SLIDE_TRANSITION_ENABLED, it) } },
    )
    private val backTransitionDirectionPreference = Preference(
        get = {
            preferences.getString(PREFERENCE_BACK_TRANSITION_DIRECTION, null)?.let { name ->
                try {
                    BackTransitionDirection.valueOf(name)
                } catch (ex: IllegalArgumentException) {
                    Log.e(TAG, "error parsing back transition direction: $name", ex)
                    BackTransitionDirection.LeftToRightOpen
                }
            } ?: BackTransitionDirection.LeftToRightOpen
        },
        set = {
            preferences.edit {
                putString(PREFERENCE_BACK_TRANSITION_DIRECTION, it.name)
            }
        },
    )

    private val preferenceKeysToPreferences = mapOf(
        PREFERENCE_AUTO_CONNECT to autoConnectPreference,
        PREFERENCE_THEME to themePreference,
        PREFERENCE_DYNAMIC_COLOR to dynamicColorPreference,
        PREFERENCE_AMOLED_BLACK to amoledBlackPreference,
        PREFERENCE_PREDICTIVE_BACK_ENABLED to predictiveBackEnabledPreference,
        PREFERENCE_SLIDE_TRANSITION_ENABLED to slideTransitionEnabledPreference,
        PREFERENCE_BACK_TRANSITION_DIRECTION to backTransitionDirectionPreference,
    )

    private val onChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key -> preferenceKeysToPreferences[key]?.refresh() }

    init {
        preferences.registerOnSharedPreferenceChangeListener(onChangeListener)
    }

    val autoConnectFlow = autoConnectPreference.flow
    var autoConnect: Boolean
        get() = autoConnectPreference.flow.value
        set(value) = autoConnectPreference.set(value)

    val themeFlow = themePreference.flow
    var theme: ThemeType?
        get() = themePreference.flow.value
        set(theme) = themePreference.set(theme)

    val dynamicColorFlow = dynamicColorPreference.flow
    var dynamicColor: Boolean
        get() = dynamicColorPreference.flow.value
        set(value) = dynamicColorPreference.set(value)

    val amoledBlackFlow = amoledBlackPreference.flow
    var amoledBlack: Boolean
        get() = amoledBlackPreference.flow.value
        set(value) = amoledBlackPreference.set(value)

    val predictiveBackEnabledFlow = predictiveBackEnabledPreference.flow
    var predictiveBackEnabled: Boolean
        get() = predictiveBackEnabledPreference.flow.value
        set(value) = predictiveBackEnabledPreference.set(value)

    val slideTransitionEnabledFlow = slideTransitionEnabledPreference.flow
    var slideTransitionEnabled: Boolean
        get() = slideTransitionEnabledPreference.flow.value
        set(value) = slideTransitionEnabledPreference.set(value)

    val backTransitionDirectionFlow = backTransitionDirectionPreference.flow
    var backTransitionDirection: BackTransitionDirection
        get() = backTransitionDirectionPreference.flow.value
        set(value) = backTransitionDirectionPreference.set(value)
}

private data class Preference<T>(val get: () -> T, val set: (T) -> Unit) {
    private val _flow = MutableStateFlow(get())
    val flow = _flow.asStateFlow()

    fun refresh() {
        _flow.value = get()
    }
}

@HiltViewModel
class ThemeViewModel @Inject constructor(preferences: Preferences) : ViewModel() {
    val themeFlow = preferences.themeFlow
    val dynamicColorFlow = preferences.dynamicColorFlow
    val amoledBlackFlow = preferences.amoledBlackFlow
    val predictiveBackEnabledFlow = preferences.predictiveBackEnabledFlow
    val slideTransitionEnabledFlow = preferences.slideTransitionEnabledFlow
    val backTransitionDirectionFlow = preferences.backTransitionDirectionFlow
}

@Composable
fun prefersDarkTheme(themeViewModel: ThemeViewModel = hiltViewModel()): Boolean =
    themeViewModel.themeFlow.collectAsState().value.prefersDarkTheme()

@Composable
fun prefersDynamicColor(themeViewModel: ThemeViewModel = hiltViewModel()): Boolean =
    themeViewModel.dynamicColorFlow.collectAsState().value

@Composable
fun prefersAmoledBlack(themeViewModel: ThemeViewModel = hiltViewModel()): Boolean =
    themeViewModel.amoledBlackFlow.collectAsState().value

@Composable
fun prefersPredictiveBack(themeViewModel: ThemeViewModel = hiltViewModel()): Boolean =
    themeViewModel.predictiveBackEnabledFlow.collectAsState().value

@Composable
fun prefersSlideTransitions(themeViewModel: ThemeViewModel = hiltViewModel()): Boolean =
    themeViewModel.slideTransitionEnabledFlow.collectAsState().value

@Composable
fun preferredBackTransitionDirection(themeViewModel: ThemeViewModel = hiltViewModel()): BackTransitionDirection =
    themeViewModel.backTransitionDirectionFlow.collectAsState().value
