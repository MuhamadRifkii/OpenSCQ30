use std::{borrow::Cow, collections::HashMap};

use openscq30_lib::settings::{self, SettingId};

use crate::{
    device_settings::{SettingUiState, SettingVariantUiState},
    fl,
};

#[derive(Clone, Copy, Debug, Default, PartialEq, Eq)]
pub struct BatterySummary {
    pub left: Option<u8>,
    pub right: Option<u8>,
    pub case_level: Option<u8>,
    pub single: Option<u8>,
    pub left_charging: bool,
    pub right_charging: bool,
}

impl BatterySummary {
    pub fn has_any(&self) -> bool {
        self.left.is_some()
            || self.right.is_some()
            || self.case_level.is_some()
            || self.single.is_some()
    }
}

pub fn supports_soundcore_ui(settings: &HashMap<SettingId, SettingUiState>) -> bool {
    [
        SettingId::AmbientSoundMode,
        SettingId::PresetEqualizerProfile,
        SettingId::VolumeAdjustments,
        SettingId::LeftSinglePress,
        SettingId::LeftDoublePress,
        SettingId::RightSinglePress,
        SettingId::RightDoublePress,
        SettingId::LimitHighVolume,
        SettingId::DualConnections,
        SettingId::BatteryLevelLeft,
        SettingId::BatteryLevelRight,
        SettingId::BatteryLevel,
    ]
    .iter()
    .any(|id| settings.contains_key(id))
}

pub fn translated_name(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<&str> {
    settings.get(&id).map(|s| s.translated_name.as_str())
}

pub fn get_toggle(settings: &HashMap<SettingId, SettingUiState>, id: SettingId) -> Option<bool> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::Toggle { value } => Some(*value),
        _ => None,
    }
}

pub fn get_select(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Select, &Cow<'static, str>)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::Select { setting, value } => Some((setting, value)),
        _ => None,
    }
}

pub fn get_optional_select(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Select, Option<&Cow<'static, str>>)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::OptionalSelect { setting, value } => {
            Some((setting, value.as_ref()))
        }
        _ => None,
    }
}

pub fn get_modifiable_select(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Select, Option<&Cow<'static, str>>)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::ModifiableSelect { setting, value } => {
            Some((setting, value.as_ref()))
        }
        _ => None,
    }
}

pub fn get_multi_select_with_remove(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Select, &[Cow<'static, str>])> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::MultiSelectWithRemove { setting, values } => {
            Some((setting, values.as_slice()))
        }
        _ => None,
    }
}

pub fn get_i32_range(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Range<i32>, i32)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::I32Range { setting, value } => Some((setting, *value)),
        _ => None,
    }
}

pub fn get_equalizer(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&settings::Equalizer, bool, &[i16])> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::Equalizer {
            setting,
            read_only,
            value,
        } => Some((setting, *read_only, value.as_slice())),
        _ => None,
    }
}

pub fn get_preset_eq(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(
    &settings::Equalizer,
    &settings::Select,
    &[Vec<i16>],
    Option<&Cow<'static, str>>,
)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::PresetEqualizerProfileSelect {
            equalizer,
            select,
            presets,
            value,
        } => Some((equalizer, select, presets.as_slice(), value.as_ref())),
        _ => None,
    }
}

pub fn get_info(settings: &HashMap<SettingId, SettingUiState>, id: SettingId) -> Option<&str> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::Information { translated_value } => Some(translated_value.as_str()),
        _ => None,
    }
}

pub fn get_import_string(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<(&str, Option<&str>)> {
    match &settings.get(&id)?.variant_state {
        SettingVariantUiState::ImportString {
            text,
            confirmation_message,
        } => Some((text.as_str(), confirmation_message.as_deref())),
        _ => None,
    }
}

pub fn has_setting(settings: &HashMap<SettingId, SettingUiState>, id: SettingId) -> bool {
    settings.contains_key(&id)
}

pub fn parse_battery_pct(raw: &str) -> Option<u8> {
    let trimmed = raw.trim();
    if let Some((num, den)) = trimmed.split_once('/') {
        let n = num.trim().parse::<u16>().ok()?;
        let d = den.trim().parse::<u16>().ok()?;
        if d == 0 {
            return None;
        }
        return Some(((n * 100) / d).min(100) as u8);
    }
    let digits: String = trimmed
        .chars()
        .take_while(|c| c.is_ascii_digit() || *c == '.')
        .collect();
    let val = digits.parse::<f32>().ok()?.round() as i32;
    Some(val.clamp(0, 100) as u8)
}

pub fn parse_db_value(raw: &str) -> Option<i32> {
    let digits: String = raw
        .trim()
        .chars()
        .skip_while(|c| !c.is_ascii_digit())
        .take_while(|c| c.is_ascii_digit())
        .collect();
    if digits.is_empty() {
        None
    } else {
        digits.parse::<i32>().ok()
    }
}

pub fn is_truthy_info(raw: &str) -> bool {
    matches!(
        raw.trim().to_lowercase().as_str(),
        "true" | "yes" | "ya" | "1" | "charging" | "detected" | "on"
    )
}

pub fn localized_select_value<'a>(
    select: &'a settings::Select,
    value: Option<&Cow<'static, str>>,
) -> Option<&'a str> {
    let target = value?;
    select
        .options
        .iter()
        .position(|opt| opt == target)
        .and_then(|idx| select.localized_options.get(idx))
        .map(String::as_str)
}

pub fn extract_battery_summary(settings: &HashMap<SettingId, SettingUiState>) -> BatterySummary {
    let left = get_info(settings, SettingId::BatteryLevelLeft).and_then(parse_battery_pct);
    let right = get_info(settings, SettingId::BatteryLevelRight).and_then(parse_battery_pct);
    let case_level = get_info(settings, SettingId::CaseBatteryLevel).and_then(parse_battery_pct);
    let single = get_info(settings, SettingId::BatteryLevel).and_then(parse_battery_pct);
    let left_charging = get_info(settings, SettingId::IsChargingLeft).is_some_and(is_truthy_info)
        || get_info(settings, SettingId::IsCharging).is_some_and(is_truthy_info);
    let right_charging =
        get_info(settings, SettingId::IsChargingRight).is_some_and(is_truthy_info);

    BatterySummary {
        left,
        right,
        case_level,
        single,
        left_charging,
        right_charging,
    }
}

pub fn current_eq_label(settings: &HashMap<SettingId, SettingUiState>) -> String {
    if let Some((_, select, _, selected)) =
        get_preset_eq(settings, SettingId::PresetEqualizerProfile)
    {
        if let Some(name) = localized_select_value(select, selected) {
            return name.to_string();
        }
        if let Some((_, Some(custom_name))) =
            get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
        {
            return custom_name.to_string();
        }
        return fl!("custom-eq");
    }
    if let Some((_, Some(custom_name))) =
        get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
    {
        return custom_name.to_string();
    }
    fl!("custom-eq")
}

pub fn active_eq_values(
    settings: &HashMap<SettingId, SettingUiState>,
) -> (Option<&settings::Equalizer>, Vec<i16>) {
    if let Some((eq_spec, select, presets, Some(selected))) =
        get_preset_eq(settings, SettingId::PresetEqualizerProfile)
        && let Some(idx) = select.options.iter().position(|opt| opt == selected)
        && let Some(preset) = presets.get(idx)
    {
        return (Some(eq_spec), preset.clone());
    }
    if let Some((eq_spec, _, values)) = get_equalizer(settings, SettingId::VolumeAdjustments) {
        return (Some(eq_spec), values.to_vec());
    }
    if let Some((eq_spec, _, _, _)) = get_preset_eq(settings, SettingId::PresetEqualizerProfile) {
        let len = eq_spec.band_hz.len();
        return (Some(eq_spec), vec![0; len]);
    }
    (None, Vec::new())
}
