@file:OptIn(ExperimentalMaterial3Api::class)

package com.oppzippy.openscq30.ui.deviceselection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.features.preferences.preferredBackTransitionDirection
import com.oppzippy.openscq30.features.preferences.prefersPredictiveBack
import com.oppzippy.openscq30.features.preferences.prefersSlideTransitions
import com.oppzippy.openscq30.lib.wrapper.ConnectionDescriptor
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.ui.deviceselection.screens.AppInfoScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.DeviceListingScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.SelectDeviceForPairingScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.SelectModelForPairingScreen
import com.oppzippy.openscq30.ui.settings.SettingsPage
import com.oppzippy.openscq30.ui.soundcore.components.ConnectedBatteryInfo
import com.oppzippy.openscq30.ui.theme.appEnterTransition
import com.oppzippy.openscq30.ui.theme.appExitTransition
import com.oppzippy.openscq30.ui.theme.appPopEnterTransition
import com.oppzippy.openscq30.ui.theme.appPopExitTransition
import com.oppzippy.openscq30.ui.theme.appPredictivePopEnterTransition
import com.oppzippy.openscq30.ui.theme.appPredictivePopExitTransition
import com.oppzippy.openscq30.ui.theme.isRtlLayout
import com.oppzippy.openscq30.ui.utils.PermissionCheck
import kotlinx.serialization.Serializable

@Composable
fun DeviceSelectionScreen(
    connectingMacAddress: String? = null,
    connectedMacAddress: String? = null,
    connectedBattery: ConnectedBatteryInfo? = null,
    onDeviceSelected: (device: PairedDevice) -> Unit,
    onDisconnectDevice: () -> Unit = {},
    viewModel: DeviceSelectionViewModel = hiltViewModel(),
) {
    val bluetoothPermission = if (Build.VERSION.SDK_INT >= 31) {
        Manifest.permission.BLUETOOTH_CONNECT
    } else {
        Manifest.permission.BLUETOOTH
    }

    val navController = rememberNavController()
    val context = LocalContext.current

    val needsLocationPermission = Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..Build.VERSION_CODES.R
    val initialPermissionsGranted = remember(context, bluetoothPermission, needsLocationPermission) {
        val hasBluetooth = ContextCompat.checkSelfPermission(
            context,
            bluetoothPermission,
        ) == PackageManager.PERMISSION_GRANTED
        val hasLocation = !needsLocationPermission || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        hasBluetooth && hasLocation
    }
    var hasRefreshedForPermission by remember { mutableStateOf(initialPermissionsGranted) }
    val refreshOnPermissionGranted = remember(viewModel) {
        {
            if (!hasRefreshedForPermission) {
                hasRefreshedForPermission = true
                viewModel.refreshPairedDevices()
            }
        }
    }

    RequiredPermissionsCheck(
        bluetoothPermission = bluetoothPermission,
        needsLocationPermission = needsLocationPermission,
        onAllPermissionsGranted = refreshOnPermissionGranted,
    ) {
        DeviceSelectionContent(
            navController = navController,
            viewModel = viewModel,
            connectingMacAddress = connectingMacAddress,
            connectedMacAddress = connectedMacAddress,
            connectedBattery = connectedBattery,
            onDeviceSelected = onDeviceSelected,
            onDisconnectDevice = onDisconnectDevice,
        )
    }
}

@Composable
private fun RequiredPermissionsCheck(
    bluetoothPermission: String,
    needsLocationPermission: Boolean,
    onAllPermissionsGranted: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (needsLocationPermission) {
        PermissionCheck(
            permission = bluetoothPermission,
            prompt = stringResource(R.string.bluetooth_permission_is_required),
        ) {
            PermissionCheck(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                prompt = stringResource(R.string.location_permission_is_required),
                onPermissionGranted = onAllPermissionsGranted,
                content = content,
            )
        }
    } else {
        PermissionCheck(
            permission = bluetoothPermission,
            prompt = stringResource(R.string.bluetooth_permission_is_required),
            onPermissionGranted = onAllPermissionsGranted,
            content = content,
        )
    }
}

@Composable
private fun DeviceSelectionContent(
    navController: NavHostController,
    viewModel: DeviceSelectionViewModel,
    connectingMacAddress: String?,
    connectedMacAddress: String?,
    connectedBattery: ConnectedBatteryInfo?,
    onDeviceSelected: (device: PairedDevice) -> Unit,
    onDisconnectDevice: () -> Unit,
) {
    val predictiveEnabled = prefersPredictiveBack()
    val slideEnabled = prefersSlideTransitions()
    val direction = preferredBackTransitionDirection()
    val isRtl = isRtlLayout()

    NavHost(
        navController = navController,
        startDestination = Screen.Connect,
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
        composable<Screen.Connect> {
            val activity = LocalActivity.current!!

            DeviceListingScreen(
                devices = viewModel.pairedDevices.collectAsState().value,
                connectingMacAddress = connectingMacAddress,
                connectedMacAddress = connectedMacAddress,
                connectedBattery = connectedBattery,
                onDeviceClick = { onDeviceSelected(it) },
                onDisconnectClick = onDisconnectDevice,
                onUnpair = { viewModel.unpair(activity, it) },
                onAddDeviceClick = { navController.navigate(Screen.SelectModelForPairing) },
                onRefreshClick = { viewModel.refreshPairedDevices() },
                onSettingsClick = { navController.navigate(Screen.Settings) },
                onInfoClick = { navController.navigate(Screen.Info) },
            )
        }

        composable<Screen.SelectDeviceForPairing> { backStackEntry ->
            val screen = backStackEntry.toRoute<Screen.SelectDeviceForPairing>()
            val activity = LocalActivity.current!!

            var isDemoMode by remember { mutableStateOf(false) }
            val devices = remember { mutableStateOf<List<ConnectionDescriptor>?>(null) }
            LaunchedEffect(screen.model, isDemoMode) {
                devices.value = viewModel.listDevices(screen.model, isDemoMode)
            }

            val devicesValue = devices.value
            SelectDeviceForPairingScreen(
                model = screen.model,
                isDemoMode = isDemoMode,
                devices = devicesValue,
                onDemoModeChange = { isDemoMode = it },
                onDescriptorSelected = {
                    viewModel.pair(
                        activity = activity,
                        pairedDevice = PairedDevice(
                            macAddress = it.macAddress,
                            model = screen.model,
                            isDemo = isDemoMode,
                        ),
                        onPaired = {
                            navController.popBackStack<Screen.Connect>(false)
                        },
                    )
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<Screen.SelectModelForPairing> {
            SelectModelForPairingScreen(
                onModelSelected = { navController.navigate(Screen.SelectDeviceForPairing(it)) },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<Screen.Info> {
            AppInfoScreen(onBackClick = { navController.popBackStack() })
        }

        composable<Screen.Settings> {
            SettingsPage(onBackClick = { navController.popBackStack() })
        }
    }

    SelectionLegacyBackHandler(
        navController = navController,
        enabled = !predictiveEnabled,
    )
}

@Composable
private fun SelectionLegacyBackHandler(navController: NavHostController, enabled: Boolean) {
    if (!enabled) return
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val canPop = currentBackStackEntry != null && navController.previousBackStackEntry != null
    BackHandler(enabled = canPop) {
        navController.popBackStack()
    }
}

@Serializable
private sealed class Screen {
    @Serializable
    data object Connect : Screen()

    @Serializable
    data object SelectModelForPairing : Screen()

    @Serializable
    data class SelectDeviceForPairing(val model: String) : Screen()

    @Serializable
    data object Info : Screen()

    @Serializable
    data object Settings : Screen()
}
