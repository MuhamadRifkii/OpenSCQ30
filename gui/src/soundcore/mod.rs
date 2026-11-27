pub mod artwork;
pub mod battery;
pub mod controls;
pub mod custom_eq;
pub mod dashboard;
pub mod dual_connections;
pub mod helpers;
pub mod more_settings;
pub mod safe_volume;
pub mod sound_effects;

use std::{borrow::Cow, collections::HashMap};

use cosmic::{Element, widget};
use openscq30_lib::settings::{self, SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl,
    soundcore::helpers::{
        get_modifiable_select, get_multi_select_with_remove, get_preset_eq, parse_db_value,
    },
};

#[derive(Clone, Copy, Debug, Default, PartialEq, Eq)]
pub enum SoundcoreRoute {
    #[default]
    Dashboard,
    SoundEffects,
    CustomEqualizer,
    Controls,
    MoreSettings,
    DualConnections,
    SafeVolume,
    QuickPresets,
    ClassicView,
}

#[derive(Clone, Debug)]
pub enum SoundcoreMessage {
    Navigate(SoundcoreRoute),
    Back,
    TogglePresetMore,
    ActivateCustomEq,
    SelectCustomEqProfile(usize),
    OpenGesturePicker(SettingId),
    CloseGesturePicker,
    AskResetControls,
    ConfirmResetControls,
    CancelDialog,
    ToggleManageDualHistory,
    AskRemoveDualDevice(String, String),
    ConfirmRemoveDualDevice,
    RequestConnectNewDevice,
    ConfirmConnectNewDevice,
    ToggleSafeVolumeInfo,
    WaveTick,
}

#[derive(Clone, Debug)]
pub enum SoundcoreDialog {
    ResetControlsConfirm,
    RemoveDualDevice { mac: String, name: String },
    ConnectNewDeviceConfirm,
}

#[derive(Default)]
pub struct SoundcoreUiState {
    pub route: SoundcoreRoute,
    pub preset_expanded: bool,
    pub last_custom_profile: Option<Cow<'static, str>>,
    pub picking_gesture: Option<SettingId>,
    pub manage_dual_history: bool,
    pub pairing_notice: bool,
    pub dialog: Option<SoundcoreDialog>,
    pub safe_volume_info: bool,
    pub displayed_db: f32,
    pub wave_phase: f32,
}

pub struct UpdateOutcome {
    pub settings_to_apply: Vec<(SettingId, Value)>,
}

impl SoundcoreUiState {
    pub fn is_classic_view(&self) -> bool {
        self.route == SoundcoreRoute::ClassicView
    }

    pub fn is_safe_volume_active(&self) -> bool {
        self.route == SoundcoreRoute::SafeVolume
    }

    pub fn can_navigate_back(&self) -> bool {
        self.picking_gesture.is_some() || self.route != SoundcoreRoute::Dashboard
    }

    pub fn navigate_back(&mut self) -> Vec<(SettingId, Value)> {
        if self.picking_gesture.take().is_some() {
            return Vec::new();
        }
        let prev = self.route;
        self.route = match prev {
            SoundcoreRoute::Dashboard => SoundcoreRoute::Dashboard,
            SoundcoreRoute::SoundEffects
            | SoundcoreRoute::Controls
            | SoundcoreRoute::MoreSettings
            | SoundcoreRoute::QuickPresets => SoundcoreRoute::Dashboard,
            SoundcoreRoute::CustomEqualizer => SoundcoreRoute::SoundEffects,
            SoundcoreRoute::DualConnections
            | SoundcoreRoute::SafeVolume
            | SoundcoreRoute::ClassicView => SoundcoreRoute::MoreSettings,
        };
        if prev == SoundcoreRoute::SafeVolume && self.route != SoundcoreRoute::SafeVolume {
            return vec![(SettingId::LimitHighVolumeDecibelMonitoring, Value::Bool(false))];
        }
        if prev == SoundcoreRoute::DualConnections {
            self.manage_dual_history = false;
            self.pairing_notice = false;
        }
        Vec::new()
    }

    pub fn on_disconnect_cleanup(&mut self) -> Vec<(SettingId, Value)> {
        if self.route == SoundcoreRoute::SafeVolume {
            self.route = SoundcoreRoute::Dashboard;
            return vec![(SettingId::LimitHighVolumeDecibelMonitoring, Value::Bool(false))];
        }
        Vec::new()
    }

    pub fn update(
        &mut self,
        message: SoundcoreMessage,
        settings: &HashMap<SettingId, SettingUiState>,
    ) -> UpdateOutcome {
        let mut settings_to_apply = Vec::new();

        if let Some((_, Some(active_custom))) =
            get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
        {
            self.last_custom_profile = Some(active_custom.clone());
        }

        match message {
            SoundcoreMessage::Navigate(next) => {
                let prev = self.route;
                self.route = next;
                if next != SoundcoreRoute::Controls {
                    self.picking_gesture = None;
                }
                if prev == SoundcoreRoute::DualConnections
                    && next != SoundcoreRoute::DualConnections
                {
                    self.manage_dual_history = false;
                    self.pairing_notice = false;
                }
                if next == SoundcoreRoute::SafeVolume && prev != SoundcoreRoute::SafeVolume {
                    self.displayed_db =
                        helpers::get_info(settings, SettingId::LimitHighVolumeCurrentDb)
                            .and_then(parse_db_value)
                            .unwrap_or(0) as f32;
                    settings_to_apply.push((
                        SettingId::LimitHighVolumeDecibelMonitoring,
                        Value::Bool(true),
                    ));
                } else if prev == SoundcoreRoute::SafeVolume
                    && next != SoundcoreRoute::SafeVolume
                {
                    settings_to_apply.push((
                        SettingId::LimitHighVolumeDecibelMonitoring,
                        Value::Bool(false),
                    ));
                }
            }
            SoundcoreMessage::Back => {
                settings_to_apply.extend(self.navigate_back());
            }
            SoundcoreMessage::TogglePresetMore => {
                self.preset_expanded = !self.preset_expanded;
            }
            SoundcoreMessage::ActivateCustomEq => {
                if let Some((select, _)) =
                    get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
                {
                    if let Some(target) = self
                        .last_custom_profile
                        .as_ref()
                        .filter(|name| select.options.contains(name))
                        .or_else(|| select.options.first())
                    {
                        settings_to_apply.push((
                            SettingId::CustomEqualizerProfile,
                            Value::OptionalString(Some(target.clone())),
                        ));
                    } else if get_preset_eq(settings, SettingId::PresetEqualizerProfile).is_some()
                    {
                        settings_to_apply.push((
                            SettingId::PresetEqualizerProfile,
                            Value::OptionalString(None),
                        ));
                    }
                } else if get_preset_eq(settings, SettingId::PresetEqualizerProfile).is_some() {
                    settings_to_apply.push((
                        SettingId::PresetEqualizerProfile,
                        Value::OptionalString(None),
                    ));
                }
            }
            SoundcoreMessage::SelectCustomEqProfile(idx) => {
                if let Some((select, _)) =
                    get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
                    && let Some(opt) = select.options.get(idx)
                {
                    self.last_custom_profile = Some(opt.clone());
                    settings_to_apply.push((
                        SettingId::CustomEqualizerProfile,
                        Value::OptionalString(Some(opt.clone())),
                    ));
                }
            }
            SoundcoreMessage::OpenGesturePicker(id) => {
                self.picking_gesture = Some(id);
            }
            SoundcoreMessage::CloseGesturePicker => {
                self.picking_gesture = None;
            }
            SoundcoreMessage::AskResetControls => {
                self.dialog = Some(SoundcoreDialog::ResetControlsConfirm);
            }
            SoundcoreMessage::ConfirmResetControls => {
                self.dialog = None;
                settings_to_apply.push((SettingId::ResetButtonsToDefault, Value::Bool(false)));
            }
            SoundcoreMessage::CancelDialog => {
                self.dialog = None;
            }
            SoundcoreMessage::ToggleManageDualHistory => {
                self.manage_dual_history = !self.manage_dual_history;
            }
            SoundcoreMessage::AskRemoveDualDevice(mac, name) => {
                self.dialog = Some(SoundcoreDialog::RemoveDualDevice { mac, name });
            }
            SoundcoreMessage::ConfirmRemoveDualDevice => {
                if let Some(SoundcoreDialog::RemoveDualDevice { mac, .. }) = self.dialog.take() {
                    settings_to_apply.push((
                        SettingId::DualConnectionsDevices,
                        Value::MultiSelectWithRemoveCommand(
                            settings::MultiSelectWithRemoveCommand::Remove(mac.into()),
                        ),
                    ));
                }
            }
            SoundcoreMessage::RequestConnectNewDevice => {
                let connected_count =
                    get_multi_select_with_remove(settings, SettingId::DualConnectionsDevices)
                        .map_or(0, |(_, values)| values.len());
                if connected_count >= 2 {
                    self.dialog = Some(SoundcoreDialog::ConnectNewDeviceConfirm);
                } else {
                    self.pairing_notice = true;
                    settings_to_apply.push((
                        SettingId::DualConnectionsConnectNewDevice,
                        Value::Bool(false),
                    ));
                }
            }
            SoundcoreMessage::ConfirmConnectNewDevice => {
                self.dialog = None;
                self.pairing_notice = true;
                settings_to_apply.push((
                    SettingId::DualConnectionsConnectNewDevice,
                    Value::Bool(false),
                ));
            }
            SoundcoreMessage::ToggleSafeVolumeInfo => {
                self.safe_volume_info = !self.safe_volume_info;
            }
            SoundcoreMessage::WaveTick => {
                self.wave_phase = (self.wave_phase + 0.11) % std::f32::consts::TAU;
                if let Some(raw_db) =
                    helpers::get_info(settings, SettingId::LimitHighVolumeCurrentDb)
                        .and_then(parse_db_value)
                {
                    let target = raw_db as f32;
                    if (self.displayed_db - target).abs() < 0.05 {
                        self.displayed_db = target;
                    } else {
                        self.displayed_db += (target - self.displayed_db) * 0.28;
                    }
                }
            }
        }

        UpdateOutcome { settings_to_apply }
    }

    pub fn dialog(&self) -> Option<Element<'_, DeviceMessage>> {
        let dlg = self.dialog.as_ref()?;
        Some(match dlg {
            SoundcoreDialog::ResetControlsConfirm => widget::dialog()
                .title(fl!("reset"))
                .body(fl!("reset-controls-confirm"))
                .primary_action(
                    widget::button::destructive(fl!("reset")).on_press(
                        DeviceMessage::Soundcore(SoundcoreMessage::ConfirmResetControls),
                    ),
                )
                .secondary_action(
                    widget::button::text(fl!("cancel")).on_press(DeviceMessage::Soundcore(
                        SoundcoreMessage::CancelDialog,
                    )),
                )
                .into(),
            SoundcoreDialog::RemoveDualDevice { name, .. } => widget::dialog()
                .title(fl!("remove-item", name = name.as_str()))
                .body(fl!("remove-item-confirm", name = name.as_str()))
                .primary_action(
                    widget::button::destructive(fl!("remove")).on_press(
                        DeviceMessage::Soundcore(SoundcoreMessage::ConfirmRemoveDualDevice),
                    ),
                )
                .secondary_action(
                    widget::button::text(fl!("cancel")).on_press(DeviceMessage::Soundcore(
                        SoundcoreMessage::CancelDialog,
                    )),
                )
                .into(),
            SoundcoreDialog::ConnectNewDeviceConfirm => widget::dialog()
                .title(fl!("connect-new-device"))
                .body(fl!("connect-new-device-disconnect-confirm"))
                .primary_action(
                    widget::button::suggested(fl!("confirm")).on_press(
                        DeviceMessage::Soundcore(SoundcoreMessage::ConfirmConnectNewDevice),
                    ),
                )
                .secondary_action(
                    widget::button::text(fl!("cancel")).on_press(DeviceMessage::Soundcore(
                        SoundcoreMessage::CancelDialog,
                    )),
                )
                .into(),
        })
    }

    pub fn view<'a>(
        &'a self,
        device_name: &'a str,
        settings: &'a HashMap<SettingId, SettingUiState>,
        has_quick_presets: bool,
    ) -> Element<'a, DeviceMessage> {
        match self.route {
            SoundcoreRoute::Dashboard => {
                dashboard::view(device_name, settings, has_quick_presets)
            }
            SoundcoreRoute::SoundEffects => {
                sound_effects::view(settings, self.preset_expanded)
            }
            SoundcoreRoute::CustomEqualizer => custom_eq::view(settings),
            SoundcoreRoute::Controls => controls::view(settings, self.picking_gesture),
            SoundcoreRoute::MoreSettings => more_settings::view(settings, has_quick_presets),
            SoundcoreRoute::DualConnections => {
                dual_connections::view(settings, self.manage_dual_history, self.pairing_notice)
            }
            SoundcoreRoute::SafeVolume => safe_volume::view(
                settings,
                self.displayed_db,
                self.wave_phase,
                self.safe_volume_info,
            ),
            SoundcoreRoute::QuickPresets | SoundcoreRoute::ClassicView => {
                dashboard::view(device_name, settings, has_quick_presets)
            }
        }
    }
}
