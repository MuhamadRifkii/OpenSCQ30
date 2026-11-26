package com.oppzippy.openscq30.ui.devicesettings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.oppzippy.openscq30.features.equalizer.storage.LegacyEqualizerProfile
import com.oppzippy.openscq30.features.preferences.preferredBackTransitionDirection
import com.oppzippy.openscq30.features.preferences.prefersPredictiveBack
import com.oppzippy.openscq30.features.preferences.prefersSlideTransitions
import com.oppzippy.openscq30.features.soundcoredevice.service.ConnectionStatus
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.QuickPreset
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.ui.devicesettings.screens.CategorySelectionScreen
import com.oppzippy.openscq30.ui.devicesettings.screens.Disconnected
import com.oppzippy.openscq30.ui.devicesettings.screens.EditQuickPresetPage
import com.oppzippy.openscq30.ui.devicesettings.screens.MigrateLegacyEqualizerProfilesScreen
import com.oppzippy.openscq30.ui.devicesettings.screens.QuickPresetsScreen
import com.oppzippy.openscq30.ui.devicesettings.screens.SettingCategoryScreen
import com.oppzippy.openscq30.ui.devicesettings.screens.StatusNotificationScreen
import com.oppzippy.openscq30.ui.settings.SettingsPage
import com.oppzippy.openscq30.ui.soundcore.isSoundcoreDashboardSupported
import com.oppzippy.openscq30.ui.soundcore.screens.ControlsScreen
import com.oppzippy.openscq30.ui.soundcore.screens.CustomEqualizerScreen
import com.oppzippy.openscq30.ui.soundcore.screens.DualConnectionsScreen
import com.oppzippy.openscq30.ui.soundcore.screens.MoreSettingsScreen
import com.oppzippy.openscq30.ui.soundcore.screens.SafeVolumeScreen
import com.oppzippy.openscq30.ui.soundcore.screens.SoundEffectsScreen
import com.oppzippy.openscq30.ui.soundcore.screens.SoundcoreDashboardScreen
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import com.oppzippy.openscq30.ui.theme.appEnterTransition
import com.oppzippy.openscq30.ui.theme.appExitTransition
import com.oppzippy.openscq30.ui.theme.appPopEnterTransition
import com.oppzippy.openscq30.ui.theme.appPopExitTransition
import com.oppzippy.openscq30.ui.theme.appPredictivePopEnterTransition
import com.oppzippy.openscq30.ui.theme.appPredictivePopExitTransition
import com.oppzippy.openscq30.ui.theme.isRtlLayout
import com.oppzippy.openscq30.ui.utils.Loading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DeviceSettingsScreen(
    onBack: () -> Unit = {},
    onDisconnect: () -> Unit = onBack,
    connectionStatus: ConnectionStatus,
    setSettingValues: (settingValues: List<Pair<String, Value>>) -> Unit,
    allSettingsFlow: Flow<List<Pair<String, Setting>>>,
    categoryIdsFlow: Flow<List<String>>,
    getSettingsInCategoryFlow: (categoryId: String) -> Flow<List<Pair<String, Setting>>>,
    quickPresetSlotsFlow: Flow<List<String?>>,
    onQuickPresetSlotChange: (Int, String?) -> Unit,
    quickPresetsFlow: Flow<List<QuickPreset>>,
    activateQuickPreset: (name: String) -> Unit,
    createQuickPreset: (name: String) -> Unit,
    deleteQuickPreset: (name: String) -> Unit,
    toggleQuickPresetSetting: (name: String, settingId: String, enabled: Boolean) -> Unit,
    featuredSettingSlotsFlow: Flow<List<String?>>,
    onFeaturedSettingSlotChange: (Int, String?) -> Unit,
    onQuickPresetLoadCurrentSettings: (String) -> Unit,
    legacyEqualizerProfilesFlow: Flow<List<LegacyEqualizerProfile>>,
    onMigrateLegacyEqualizerProfile: (LegacyEqualizerProfile) -> Unit,
) {
    when (connectionStatus) {
        is ConnectionStatus.Connected -> {
            DeviceSettingsNavigationScreen(
                connectionStatus = connectionStatus,
                onBack = onBack,
                onDisconnect = onDisconnect,
                setSettingValues = setSettingValues,
                allSettingsFlow = allSettingsFlow,
                categoryIdsFlow = categoryIdsFlow,
                getSettingsInCategoryFlow = getSettingsInCategoryFlow,
                quickPresetSlotsFlow = quickPresetSlotsFlow,
                onQuickPresetSlotChange = onQuickPresetSlotChange,
                quickPresetsFlow = quickPresetsFlow,
                activateQuickPreset = activateQuickPreset,
                createQuickPreset = createQuickPreset,
                deleteQuickPreset = deleteQuickPreset,
                toggleQuickPresetSetting = toggleQuickPresetSetting,
                featuredSettingSlotsFlow = featuredSettingSlotsFlow,
                onFeaturedSettingSlotChange = onFeaturedSettingSlotChange,
                onQuickPresetLoadCurrentSettings = onQuickPresetLoadCurrentSettings,
                legacyEqualizerProfilesFlow = legacyEqualizerProfilesFlow,
                onMigrateLegacyEqualizerProfile = onMigrateLegacyEqualizerProfile,
            )
        }

        ConnectionStatus.AwaitingConnection -> Loading()

        is ConnectionStatus.Connecting -> Loading()

        ConnectionStatus.Disconnected -> Disconnected()
    }
}

@Composable
private fun DeviceSettingsNavigationScreen(
    connectionStatus: ConnectionStatus.Connected,
    onBack: () -> Unit = {},
    onDisconnect: () -> Unit = onBack,
    setSettingValues: (settingValues: List<Pair<String, Value>>) -> Unit,
    categoryIdsFlow: Flow<List<String>>,
    allSettingsFlow: Flow<List<Pair<String, Setting>>>,
    getSettingsInCategoryFlow: (categoryId: String) -> Flow<List<Pair<String, Setting>>>,
    quickPresetSlotsFlow: Flow<List<String?>>,
    onQuickPresetSlotChange: (Int, String?) -> Unit,
    quickPresetsFlow: Flow<List<QuickPreset>>,
    activateQuickPreset: (name: String) -> Unit,
    createQuickPreset: (name: String) -> Unit,
    deleteQuickPreset: (name: String) -> Unit,
    toggleQuickPresetSetting: (name: String, settingId: String, enabled: Boolean) -> Unit,
    featuredSettingSlotsFlow: Flow<List<String?>>,
    onFeaturedSettingSlotChange: (Int, String?) -> Unit,
    onQuickPresetLoadCurrentSettings: (String) -> Unit,
    legacyEqualizerProfilesFlow: Flow<List<LegacyEqualizerProfile>>,
    onMigrateLegacyEqualizerProfile: (LegacyEqualizerProfile) -> Unit,
) {
    val categoryIds by categoryIdsFlow.collectAsStateWithValue(emptyList())
    val initialSettings = remember(allSettingsFlow) {
        (allSettingsFlow as? StateFlow<List<Pair<String, Setting>>>)?.value?.takeIf { it.isNotEmpty() }
    }
    val allSettingsState by allSettingsFlow.collectAsState(initialSettings)
    val allSettings = allSettingsState
    if (allSettings.isNullOrEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(SoundcoreColors.Background),
        )
        return
    }
    val settingsMap = remember(allSettings) { allSettings.toMap() }
    val deviceModel = remember(connectionStatus) { connectionStatus.deviceManager.device.model() }
    val useSoundcoreUi = remember(deviceModel, settingsMap) {
        isSoundcoreDashboardSupported(deviceModel, settingsMap)
    }
    val predictiveEnabled = prefersPredictiveBack()
    val slideEnabled = prefersSlideTransitions()
    val direction = preferredBackTransitionDirection()
    val isRtl = isRtlLayout()

    val listedScreens = remember(categoryIds) {
        buildList {
            categoryIds.forEach { add(Screen.SettingsCategory(it).screenInfo()) }
            add(Screen.QuickPresets.screenInfo)
            add(Screen.StatusNotification.screenInfo)
            add(Screen.MigrateLegacyEqualizerProfiles.screenInfo)
        }
    }

    val navController = rememberNavController()

    val setSingleSetting = remember(setSettingValues) {
        { settingId: String, value: Value ->
            setSettingValues(listOf(settingId to value))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.ScreenSelection,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { appEnterTransition(slideEnabled, direction, isRtl) },
        exitTransition = { appExitTransition(slideEnabled, direction, isRtl) },
        popEnterTransition = { appPopEnterTransition(slideEnabled, direction, null, isRtl) },
        popExitTransition = { appPopExitTransition(slideEnabled, direction, null, isRtl) },
        predictivePopEnterTransition = { swipeEdge ->
            appPredictivePopEnterTransition(slideEnabled, predictiveEnabled, direction, swipeEdge, isRtl)
        },
        predictivePopExitTransition = { swipeEdge ->
            appPredictivePopExitTransition(slideEnabled, predictiveEnabled, direction, swipeEdge, isRtl)
        },
    ) {
        composable<Screen.ScreenSelection> {
            if (useSoundcoreUi) {
                SoundcoreDashboardScreen(
                    deviceModel = deviceModel,
                    settings = settingsMap,
                    onSetSetting = setSingleSetting,
                    onBack = onBack,
                    onOpenSoundEffects = { navController.navigate(Screen.SoundEffects) },
                    onOpenControls = { navController.navigate(Screen.Controls) },
                    onOpenMoreSettings = { navController.navigate(Screen.MoreSettings) },
                    onOpenQuickPresets = { navController.navigate(Screen.QuickPresets) },
                )
            } else {
                CategorySelectionScreen(
                    screens = listedScreens,
                    title = translateDeviceModel(deviceModel),
                    onBack = onBack,
                    onNavigation = { screen ->
                        navController.navigate(screen) {
                            popUpTo(Screen.ScreenSelection)
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable<Screen.SoundEffects> {
            SoundEffectsScreen(
                settings = settingsMap,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
                onOpenCustomEq = { navController.navigate(Screen.CustomEqualizer) },
            )
        }

        composable<Screen.CustomEqualizer> {
            CustomEqualizerScreen(
                settings = settingsMap,
                hasImportExport = "equalizerImportExport" in categoryIds,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
                onOpenImportExport = {
                    navController.navigate(Screen.SettingsCategory("equalizerImportExport"))
                },
            )
        }

        composable<Screen.Controls> {
            ControlsScreen(
                settings = settingsMap,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.MoreSettings> {
            MoreSettingsScreen(
                deviceModel = deviceModel,
                settings = settingsMap,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
                onOpenSafeVolume = { navController.navigate(Screen.SafeVolume) },
                onOpenDualConnections = { navController.navigate(Screen.DualConnections) },
                onOpenStatusNotification = { navController.navigate(Screen.StatusNotification) },
                onOpenQuickPresets = { navController.navigate(Screen.QuickPresets) },
                onOpenClassicView = { navController.navigate(Screen.ClassicCategories) },
                onOpenAppSettings = { navController.navigate(Screen.AppSettings) },
                onDisconnect = onDisconnect,
            )
        }

        composable<Screen.DualConnections> {
            DualConnectionsScreen(
                settings = settingsMap,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.SafeVolume> {
            SafeVolumeScreen(
                settings = settingsMap,
                onSetSetting = setSingleSetting,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.ClassicCategories> {
            CategorySelectionScreen(
                screens = listedScreens,
                onBack = { navController.popBackStack() },
                onNavigation = { screen ->
                    navController.navigate(screen)
                },
            )
        }

        composable<Screen.AppSettings> {
            SettingsPage(onBackClick = { navController.popBackStack() })
        }

        composable<Screen.SettingsCategory> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.SettingsCategory>()
            val settingsFlow = remember(route.categoryId, getSettingsInCategoryFlow) {
                getSettingsInCategoryFlow(route.categoryId)
            }
            val settings by settingsFlow.collectAsStateWithValue(emptyList())
            SettingCategoryScreen(
                deviceModel = deviceModel,
                categoryId = route.categoryId,
                settings = settings,
                setSettings = setSettingValues,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.QuickPresets> {
            val quickPresets by quickPresetsFlow.collectAsStateWithValue(emptyList())
            QuickPresetsScreen(
                quickPresets = quickPresets,
                onActivate = activateQuickPreset,
                onCreate = createQuickPreset,
                onEdit = { navController.navigate(Screen.EditQuickPreset(it)) },
                onDelete = deleteQuickPreset,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.EditQuickPreset> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.EditQuickPreset>()
            val quickPresets by quickPresetsFlow.collectAsStateWithValue(emptyList())
            val quickPreset = quickPresets.find { it.name == route.name }
            if (quickPreset != null) {
                EditQuickPresetPage(
                    settings = settingsMap,
                    quickPreset = quickPreset,
                    onToggleSetting = toggleQuickPresetSetting,
                    onLoadCurrentSettings = { onQuickPresetLoadCurrentSettings(quickPreset.name) },
                    onBack = { navController.popBackStack() },
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(SoundcoreColors.Background),
                )
            }
        }

        composable<Screen.StatusNotification> {
            val featuredSettingSlots by featuredSettingSlotsFlow.collectAsStateWithValue(emptyList())
            val quickPresets by quickPresetsFlow.collectAsStateWithValue(emptyList())
            val quickPresetSlots by quickPresetSlotsFlow.collectAsStateWithValue(emptyList())
            val settingIds = remember(allSettings) { allSettings.map { it.first } }
            val quickPresetNames = remember(quickPresets) { quickPresets.map { it.name } }
            StatusNotificationScreen(
                settingIds = settingIds,
                featuredSettingSlots = featuredSettingSlots,
                onFeaturedSettingSlotChange = onFeaturedSettingSlotChange,
                quickPresets = quickPresetNames,
                quickPresetSlots = quickPresetSlots,
                onQuickPresetSlotChange = onQuickPresetSlotChange,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.MigrateLegacyEqualizerProfiles> {
            val legacyEqualizerProfiles by legacyEqualizerProfilesFlow.collectAsStateWithValue(emptyList())
            MigrateLegacyEqualizerProfilesScreen(
                legacyEqualizerProfiles = legacyEqualizerProfiles,
                onMigrateLegacyEqualizerProfile = onMigrateLegacyEqualizerProfile,
                onBack = { navController.popBackStack() },
            )
        }
    }

    LegacyBackHandler(
        navController = navController,
        enabled = !predictiveEnabled,
    )
}

@Composable
private fun LegacyBackHandler(navController: NavHostController, enabled: Boolean) {
    if (!enabled) return
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val canPop = currentBackStackEntry != null && navController.previousBackStackEntry != null
    BackHandler(enabled = canPop) {
        navController.popBackStack()
    }
}

@Composable
private fun <T> Flow<T>.collectAsStateWithValue(fallback: T): State<T> {
    val initial = remember(this) { (this as? StateFlow<T>)?.value ?: fallback }
    return collectAsState(initial = initial)
}
