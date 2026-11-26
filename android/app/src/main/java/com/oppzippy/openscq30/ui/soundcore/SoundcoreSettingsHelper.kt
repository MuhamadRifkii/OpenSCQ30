package com.oppzippy.openscq30.ui.soundcore

import com.oppzippy.openscq30.lib.wrapper.Setting

object SoundcoreSettingIds {
    const val AMBIENT_SOUND_MODE = "ambientSoundMode"
    const val TRANSPARENCY_MODE = "transparencyMode"
    const val NOISE_CANCELING_MODE = "noiseCancelingMode"
    const val ADAPTIVE_NOISE_CANCELING = "adaptiveNoiseCanceling"
    const val MANUAL_NOISE_CANCELING = "manualNoiseCanceling"
    const val MULTI_SCENE_NOISE_CANCELING = "multiSceneNoiseCanceling"
    const val WIND_NOISE_SUPPRESSION = "windNoiseSuppression"
    const val WIND_NOISE_DETECTED = "windNoiseDetected"
    const val ADAPTIVE_ANC_SENSITIVITY = "adaptiveNoiseCancelingSensitivityLevel"
    const val REAL_TIME_ADAPTIVE_ANC = "realTimeAdaptiveNoiseCanceling"

    const val PRESET_EQUALIZER_PROFILE = "presetEqualizerProfile"
    const val CUSTOM_EQUALIZER_PROFILE = "customEqualizerProfile"
    const val VOLUME_ADJUSTMENTS = "volumeAdjustments"

    const val SPATIAL_AUDIO = "spatialAudio"
    const val SPATIAL_AUDIO_MODE = "spatialAudioMode"
    const val SURROUND_SOUND = "surroundSound"

    const val LEFT_SINGLE_PRESS = "leftSinglePress"
    const val RIGHT_SINGLE_PRESS = "rightSinglePress"
    const val LEFT_DOUBLE_PRESS = "leftDoublePress"
    const val RIGHT_DOUBLE_PRESS = "rightDoublePress"
    const val LEFT_TRIPLE_PRESS = "leftTriplePress"
    const val RIGHT_TRIPLE_PRESS = "rightTriplePress"
    const val LEFT_LONG_PRESS = "leftLongPress"
    const val RIGHT_LONG_PRESS = "rightLongPress"
    const val NORMAL_MODE_IN_CYCLE = "normalModeInCycle"
    const val TRANSPARENCY_MODE_IN_CYCLE = "transparencyModeInCycle"
    const val NOISE_CANCELING_MODE_IN_CYCLE = "noiseCancelingModeInCycle"
    const val RESET_BUTTONS_TO_DEFAULT = "resetButtonsToDefault"

    const val LIMIT_HIGH_VOLUME = "limitHighVolume"
    const val LIMIT_HIGH_VOLUME_DB_LIMIT = "limitHighVolumeDbLimit"
    const val LIMIT_HIGH_VOLUME_REFRESH_RATE = "limitHighVolumeRefreshRate"
    const val LIMIT_HIGH_VOLUME_DECIBEL_MONITORING = "limitHighVolumeDecibelMonitoring"
    const val LIMIT_HIGH_VOLUME_CURRENT_DB = "limitHighVolumeCurrentDb"

    const val DUAL_CONNECTIONS = "dualConnections"
    const val DUAL_CONNECTIONS_DEVICES = "dualConnectionsDevices"
    const val DUAL_CONNECTIONS_CONNECT_NEW_DEVICE = "dualConnectionsConnectNewDevice"

    const val LDAC = "ldac"
    const val GAMING_MODE = "gamingMode"
    const val TOUCH_TONE = "touchTone"
    const val LOW_BATTERY_PROMPT = "lowBatteryPrompt"
    const val AUTO_POWER_OFF = "autoPowerOff"

    const val BATTERY_LEVEL_LEFT = "batteryLevelLeft"
    const val BATTERY_LEVEL_RIGHT = "batteryLevelRight"
    const val CASE_BATTERY_LEVEL = "caseBatteryLevel"
    const val IS_CHARGING_LEFT = "isChargingLeft"
    const val IS_CHARGING_RIGHT = "isChargingRight"
    const val FIRMWARE_VERSION_LEFT = "firmwareVersionLeft"
    const val FIRMWARE_VERSION_RIGHT = "firmwareVersionRight"
    const val SERIAL_NUMBER = "serialNumber"
    const val TWS_STATUS = "twsStatus"
}

inline fun <reified T : Setting> Map<String, Setting>.getSetting(id: String): T? = this[id] as? T

fun parseBatteryFraction(info: Setting.InformationSetting?): Float? {
    if (info == null) return null
    val slashParts = info.value.split("/")
    if (slashParts.size == 2) {
        val num = slashParts[0].trim().toFloatOrNull()
        val den = slashParts[1].trim().toFloatOrNull()
        if (num != null && den != null && den > 0f) {
            return (num / den).coerceIn(0f, 1f)
        }
    }
    val pct = Regex("(\\d+)").find(info.translatedValue)?.groupValues?.getOrNull(1)?.toFloatOrNull()
    return pct?.let { (it / 100f).coerceIn(0f, 1f) }
}

fun isCharging(info: Setting.InformationSetting?): Boolean {
    if (info == null) return false
    return info.value.equals("Yes", ignoreCase = true) || info.value.equals("true", ignoreCase = true)
}

fun isSoundcoreDashboardSupported(deviceModel: String, settings: Map<String, Setting>): Boolean =
    deviceModel.startsWith("Soundcore") &&
        (
            settings.containsKey(SoundcoreSettingIds.AMBIENT_SOUND_MODE) ||
                settings.containsKey(SoundcoreSettingIds.PRESET_EQUALIZER_PROFILE)
            )
