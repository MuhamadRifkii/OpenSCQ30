use std::{borrow::Cow, collections::HashMap};

use cosmic::{
    Element,
    iced::{Alignment, Background, Color, Length},
    widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl, icons,
    soundcore::{
        SoundcoreMessage, SoundcoreRoute,
        helpers::{
            get_info, get_select, get_toggle, has_setting, localized_select_value, translated_name,
        },
        sound_effects::subpage_header,
    },
    theme::{
        SOUNDCORE_CORAL, centered_page, nav_row, secondary_text, soundcore_divider,
        soundcore_grouped_card,
    },
};

pub fn view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    has_quick_presets: bool,
) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(6).spacing(14);
    col = col.push(subpage_header(fl!("more-settings")));

    // Feature links card
    let mut feature_rows: Vec<Element<'a, DeviceMessage>> = Vec::new();
    if has_setting(settings, SettingId::LimitHighVolume) {
        let subtitle = if get_toggle(settings, SettingId::LimitHighVolume).unwrap_or(false) {
            Some(Cow::Owned(fl!("enabled")))
        } else {
            None
        };
        feature_rows.push(nav_row(
            fl!("safe-volume"),
            None,
            subtitle,
            Some(icons::tinted_primary(icons::speaker(), 20)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(SoundcoreRoute::SafeVolume)),
        ));
    }

    if has_setting(settings, SettingId::DualConnections) {
        let enabled = get_toggle(settings, SettingId::DualConnections).unwrap_or(false);
        let state_text = if enabled {
            fl!("enabled")
        } else {
            fl!("disabled")
        };
        feature_rows.push(nav_row(
            fl!("dual-connections"),
            None,
            Some(Cow::Owned(state_text)),
            Some(icons::tinted_primary(icons::bluetooth(), 20)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(
                SoundcoreRoute::DualConnections,
            )),
        ));
    }

    if has_quick_presets {
        feature_rows.push(nav_row(
            fl!("quick-presets"),
            None,
            None,
            Some(icons::tinted_primary(icons::bolt(), 20)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(SoundcoreRoute::QuickPresets)),
        ));
    }

    if !feature_rows.is_empty() {
        col = col.push(grouped_list(feature_rows));
    }

    // Device toggles & dropdowns
    let mut pref_rows: Vec<Element<'a, DeviceMessage>> = Vec::new();
    let toggle_ids = [
        SettingId::Ldac,
        SettingId::GamingMode,
        SettingId::TouchLock,
        SettingId::TouchTone,
        SettingId::LowBatteryPrompt,
        SettingId::WearingDetection,
        SettingId::AutoPlayPause,
        SettingId::SoundLeakCompensation,
        SettingId::VoicePrompt,
        SettingId::SideTone,
        SettingId::DolbyAudio,
    ];
    for id in toggle_ids {
        if let Some(checked) = get_toggle(settings, id) {
            let label = translated_name(settings, id)
                .map(str::to_string)
                .unwrap_or_else(|| format!("{id:?}"));
            pref_rows.push(
                widget::container(
                    widget::row![
                        widget::text::body(label).size(15).width(Length::Fill),
                        widget::toggler(checked).on_toggle(move |v| {
                            DeviceMessage::SetSetting(id, Value::Bool(v))
                        }),
                    ]
                    .spacing(12)
                    .align_y(Alignment::Center)
                    .width(Length::Fill),
                )
                .padding([12, 16])
                .into(),
            );
        }
    }

    let select_ids = [SettingId::AutoPowerOff, SettingId::AutoPowerOffPrompt];
    for id in select_ids {
        if let Some((select, current)) = get_select(settings, id) {
            let selected_idx = select.options.iter().position(|o| o == current);
            let label = translated_name(settings, id)
                .map(str::to_string)
                .unwrap_or_else(|| format!("{id:?}"));
            let opts = select.options.to_owned();
            let val_preview = localized_select_value(select, Some(current))
                .unwrap_or("")
                .to_string();
            let _ = val_preview;
            pref_rows.push(
                widget::container(
                    widget::row![
                        widget::text::body(label).size(15).width(Length::Fill),
                        widget::dropdown(&select.localized_options, selected_idx, move |idx| {
                            DeviceMessage::SetSetting(id, Value::String(opts[idx].clone()))
                        }),
                    ]
                    .spacing(12)
                    .align_y(Alignment::Center)
                    .width(Length::Fill),
                )
                .padding([10, 16])
                .into(),
            );
        }
    }

    if !pref_rows.is_empty() {
        col = col.push(grouped_list(pref_rows));
    }

    // Classic View fallback card
    col = col.push(soundcore_grouped_card(nav_row(
        fl!("all-settings-classic"),
        Some(Cow::Owned(fl!("all-settings-classic-subtitle"))),
        None,
        Some(icons::tinted_primary(icons::settings(), 20)),
        DeviceMessage::Soundcore(SoundcoreMessage::Navigate(SoundcoreRoute::ClassicView)),
    )));

    // Device Info card
    let mut info_rows: Vec<Element<'a, DeviceMessage>> = Vec::new();
    let info_ids = [
        (SettingId::FirmwareVersionLeft, false),
        (SettingId::FirmwareVersionRight, false),
        (SettingId::FirmwareVersion, false),
        (SettingId::CaseFirmwareVersion, false),
        (SettingId::SerialNumber, true),
        (SettingId::CaseSerialNumber, true),
        (SettingId::TwsStatus, false),
        (SettingId::HostDevice, false),
    ];
    for (id, copyable) in info_ids {
        if let Some(val) = get_info(settings, id) {
            let label = translated_name(settings, id)
                .map(str::to_string)
                .unwrap_or_else(|| format!("{id:?}"));
            let val_owned = val.to_string();
            info_rows.push(
                widget::container(
                    widget::row![
                        widget::text::body(label).size(14).width(Length::Fill),
                        secondary_text(val, 13.0),
                        copyable.then(|| {
                            widget::button::custom(icons::tinted_secondary(icons::copy(), 16))
                                .class(crate::theme::transparent_row_button_style(10.0))
                                .padding([6, 8])
                                .on_press(DeviceMessage::CopyToClipboard(val_owned))
                        }),
                    ]
                    .spacing(8)
                    .align_y(Alignment::Center)
                    .width(Length::Fill),
                )
                .padding([12, 16])
                .into(),
            );
        }
    }

    if !info_rows.is_empty() {
        col = col.push(grouped_list(info_rows));
    }

    // Disconnect CTA
    col = col.push(
        widget::button::custom(
            widget::text::body(fl!("disconnect"))
                .size(15)
                .font(cosmic::font::semibold())
                .align_x(cosmic::iced::alignment::Horizontal::Center)
                .width(Length::Fill),
        )
        .class(cosmic::widget::button::ButtonClass::Custom {
            active: Box::new(|_focused, _theme| cosmic::widget::button::Style {
                shadow_offset: cosmic::iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(Color {
                    a: 0.14,
                    ..SOUNDCORE_CORAL
                })),
                overlay: None,
                border_radius: 18.0.into(),
                border_width: 1.0,
                border_color: Color {
                    a: 0.32,
                    ..SOUNDCORE_CORAL
                },
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(SOUNDCORE_CORAL),
                text_color: Some(SOUNDCORE_CORAL),
            }),
            disabled: Box::new(|_theme| cosmic::widget::button::Style {
                shadow_offset: cosmic::iced::Vector::new(0.0, 0.0),
                background: None,
                overlay: None,
                border_radius: 18.0.into(),
                border_width: 0.5,
                border_color: Color { a: 0.2, ..SOUNDCORE_CORAL },
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(SOUNDCORE_CORAL),
                text_color: Some(SOUNDCORE_CORAL),
            }),
            hovered: Box::new(|_focused, _theme| cosmic::widget::button::Style {
                shadow_offset: cosmic::iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(Color { a: 0.24, ..SOUNDCORE_CORAL })),
                overlay: None,
                border_radius: 18.0.into(),
                border_width: 1.0,
                border_color: SOUNDCORE_CORAL,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(SOUNDCORE_CORAL),
                text_color: Some(SOUNDCORE_CORAL),
            }),
            pressed: Box::new(|_focused, _theme| cosmic::widget::button::Style {
                shadow_offset: cosmic::iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(Color { a: 0.34, ..SOUNDCORE_CORAL })),
                overlay: None,
                border_radius: 18.0.into(),
                border_width: 1.0,
                border_color: SOUNDCORE_CORAL,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(SOUNDCORE_CORAL),
                text_color: Some(SOUNDCORE_CORAL),
            }),
        })
        .padding([14, 16])
        .width(Length::Fill)
        .on_press(DeviceMessage::Disconnect),
    );

    centered_page(col.padding([8, 0]))
}

fn grouped_list<'a>(rows: Vec<Element<'a, DeviceMessage>>) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(rows.len() * 2);
    for (idx, row) in rows.into_iter().enumerate() {
        if idx > 0 {
            col = col.push(soundcore_divider());
        }
        col = col.push(row);
    }
    soundcore_grouped_card(col).into()
}
