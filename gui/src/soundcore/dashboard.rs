use std::{borrow::Cow, collections::HashMap};

use cosmic::{
    Element,
    iced::{self, Alignment, Background, Border, Color, Length},
    theme, widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl, icons,
    soundcore::{
        SoundcoreMessage, SoundcoreRoute, artwork, battery,
        helpers::{
            current_eq_label, extract_battery_summary, get_i32_range, get_info, get_select,
            get_toggle, has_setting, is_truthy_info, localized_select_value,
        },
    },
    theme::{
        SoundcorePalette, centered_page, nav_row, pill_button_style, secondary_text,
        soundcore_card, soundcore_divider, soundcore_grouped_card,
    },
};

pub fn view<'a>(
    device_name: &'a str,
    settings: &'a HashMap<SettingId, SettingUiState>,
    has_quick_presets: bool,
) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(8).spacing(14);

    col = col.push(hero_header(device_name, settings));

    if let Some(ambient) = ambient_sound_card(settings) {
        col = col.push(ambient);
    }

    if has_setting(settings, SettingId::PresetEqualizerProfile)
        || has_setting(settings, SettingId::VolumeAdjustments)
        || has_setting(settings, SettingId::SpatialAudio)
        || has_setting(settings, SettingId::SurroundSound)
    {
        let eq_label = current_eq_label(settings);
        let card = soundcore_grouped_card(nav_row(
            fl!("sound-effects"),
            Some(Cow::Owned(eq_label)),
            None,
            Some(icons::tinted_primary(icons::graphic_eq(), 22)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(
                SoundcoreRoute::SoundEffects,
            )),
        ));
        col = col.push(card);
    }

    if has_any_control(settings) {
        let card = soundcore_grouped_card(nav_row(
            fl!("controls"),
            Some(Cow::Owned(fl!("customize-touch-controls"))),
            None,
            Some(icons::tinted_primary(icons::noise_control(), 22)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(SoundcoreRoute::Controls)),
        ));
        col = col.push(card);
    }

    if let Some(toggles) = quick_toggles_card(settings) {
        col = col.push(toggles);
    }

    if has_quick_presets {
        let card = soundcore_grouped_card(nav_row(
            fl!("quick-presets"),
            Some(Cow::Owned(fl!("quick-presets-subtitle"))),
            None,
            Some(icons::tinted_primary(icons::bolt(), 22)),
            DeviceMessage::Soundcore(SoundcoreMessage::Navigate(SoundcoreRoute::QuickPresets)),
        ));
        col = col.push(card);
    }

    let more_card = soundcore_grouped_card(nav_row(
        fl!("more-settings"),
        Some(Cow::Owned(fl!("more-settings-subtitle"))),
        None,
        Some(icons::tinted_primary(icons::settings(), 22)),
        DeviceMessage::Soundcore(SoundcoreMessage::Navigate(
            SoundcoreRoute::MoreSettings,
        )),
    ));
    col = col.push(more_card);

    centered_page(col.padding([8, 0]))
}

fn hero_header<'a>(
    device_name: &'a str,
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Element<'a, DeviceMessage> {
    let battery_summary = extract_battery_summary(settings);
    let ldac_enabled = get_toggle(settings, SettingId::Ldac).unwrap_or(false);

    let title_row = widget::row::with_capacity(2)
        .push(widget::text::title2(device_name).font(cosmic::font::bold()))
        .push_maybe(ldac_enabled.then(|| {
            widget::container(widget::text::caption("LDAC").size(10).font(cosmic::font::bold()))
                .padding([3, 7])
                .class(theme::Container::custom(|theme| {
                    let p = SoundcorePalette::from_theme(theme);
                    widget::container::Style {
                        background: Some(Background::Color(Color { a: 0.16, ..p.accent })),
                        border: Border {
                            color: Color { a: 0.45, ..p.accent },
                            width: 1.0,
                            radius: 6.0.into(),
                        },
                        text_color: Some(p.accent),
                        ..Default::default()
                    }
                }))
        }))
        .spacing(8)
        .align_y(Alignment::Center);

    let mut content = widget::column::with_capacity(3)
        .push(artwork::earbuds_artwork(180.0))
        .push(title_row)
        .spacing(12)
        .align_x(Alignment::Center)
        .width(Length::Fill);

    if battery_summary.has_any() {
        content = content.push(battery::battery_strip(&battery_summary));
    }

    widget::container(content)
        .width(Length::Fill)
        .padding([8, 0])
        .into()
}

fn ambient_sound_card<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    let (select, current_mode) = get_select(settings, SettingId::AmbientSoundMode)?;

    let mut circles = widget::row::with_capacity(select.options.len()).spacing(12);
    for (idx, opt) in select.options.iter().enumerate() {
        let label = select
            .localized_options
            .get(idx)
            .map(String::as_str)
            .unwrap_or(opt.as_ref());
        let selected = opt == current_mode;
        let icon_handle = match opt.as_ref() {
            "Normal" => icons::person(),
            "Transparency" => icons::speaker(),
            _ => icons::noise_control(),
        };

        let circle = widget::button::custom(
            widget::container(if selected {
                icons::tinted_on_accent(icon_handle, 24)
            } else {
                icons::tinted_primary(icon_handle, 24)
            })
            .center(Length::Fixed(52.0)),
        )
        .class(pill_button_style(selected, false))
        .padding(0)
        .width(Length::Fixed(52.0))
        .height(Length::Fixed(52.0))
        .on_press(DeviceMessage::SetSetting(
            SettingId::AmbientSoundMode,
            Value::String(opt.clone()),
        ));

        let item = widget::column![
            circle,
            widget::text::caption(label)
                .size(12)
                .align_x(iced::alignment::Horizontal::Center),
        ]
        .spacing(6)
        .align_x(Alignment::Center)
        .width(Length::Fill);

        circles = circles.push(item);
    }

    let mut body = widget::column::with_capacity(6)
        .push(
            widget::row![
                icons::tinted_primary(icons::noise_control(), 20),
                widget::text::title4(fl!("ambient-sound")).font(cosmic::font::semibold()),
            ]
            .spacing(10)
            .align_y(Alignment::Center),
        )
        .push(circles)
        .spacing(16);

    if current_mode.as_ref() == "NoiseCanceling" {
        if let Some(anc_sub) = noise_canceling_controls(settings) {
            body = body.push(soundcore_divider()).push(anc_sub);
        }
    } else if current_mode.as_ref() == "Transparency" {
        if let Some(trans_sub) = transparency_controls(settings) {
            body = body.push(soundcore_divider()).push(trans_sub);
        }
    }

    if current_mode.as_ref() != "Normal"
        && let Some(wind_enabled) = get_toggle(settings, SettingId::WindNoiseSuppression)
    {
        let wind_detected =
            get_info(settings, SettingId::WindNoiseDetected).is_some_and(is_truthy_info);
        let label_col = widget::column::with_capacity(2)
            .push(widget::text::body(fl!("wind-noise-reduction")).size(14))
            .push_maybe(wind_detected.then(|| {
                crate::theme::accent_text(fl!("wind-detected"), 12.0)
            }))
            .spacing(2)
            .width(Length::Fill);

        let wind_row = widget::row![
            label_col,
            widget::toggler(wind_enabled).on_toggle(|enabled| {
                DeviceMessage::SetSetting(
                    SettingId::WindNoiseSuppression,
                    Value::Bool(enabled),
                )
            }),
        ]
        .spacing(12)
        .align_y(Alignment::Center)
        .width(Length::Fill);

        body = body.push(soundcore_divider()).push(wind_row);
    }

    Some(soundcore_card(body).into())
}

fn noise_canceling_controls<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    let mut col = widget::column::with_capacity(4).spacing(12);
    let mut has_any = false;

    if let Some((select, mode)) = get_select(settings, SettingId::NoiseCancelingMode) {
        has_any = true;
        let mut pills = widget::row::with_capacity(select.options.len()).spacing(8);
        for (idx, opt) in select.options.iter().enumerate() {
            let label = select
                .localized_options
                .get(idx)
                .map(String::as_str)
                .unwrap_or(opt.as_ref());
            let selected = opt == mode;
            pills = pills.push(
                widget::button::custom(
                    widget::text::body(label)
                        .size(13)
                        .align_x(iced::alignment::Horizontal::Center)
                        .width(Length::Fill),
                )
                .class(pill_button_style(selected, false))
                .padding([8, 12])
                .width(Length::Fill)
                .on_press(DeviceMessage::SetSetting(
                    SettingId::NoiseCancelingMode,
                    Value::String(opt.clone()),
                )),
            );
        }
        col = col.push(pills);

        if mode.as_ref() == "Manual" {
            if let Some((range, level)) = get_i32_range(settings, SettingId::ManualNoiseCanceling)
            {
                col = col.push(level_selector(
                    fl!("anc-level"),
                    SettingId::ManualNoiseCanceling,
                    range,
                    level,
                ));
            }
        } else if mode.as_ref() == "MultiScene" {
            if let Some((scene_select, current_scene)) =
                get_select(settings, SettingId::MultiSceneNoiseCanceling)
            {
                let mut scene_row =
                    widget::row::with_capacity(scene_select.options.len()).spacing(8);
                for (idx, opt) in scene_select.options.iter().enumerate() {
                    let label = scene_select
                        .localized_options
                        .get(idx)
                        .map(String::as_str)
                        .unwrap_or(opt.as_ref());
                    let selected = opt == current_scene;
                    scene_row = scene_row.push(
                        widget::button::custom(
                            widget::text::body(label)
                                .size(12)
                                .align_x(iced::alignment::Horizontal::Center)
                                .width(Length::Fill),
                        )
                        .class(pill_button_style(selected, false))
                        .padding([8, 10])
                        .width(Length::Fill)
                        .on_press(DeviceMessage::SetSetting(
                            SettingId::MultiSceneNoiseCanceling,
                            Value::String(opt.clone()),
                        )),
                    );
                }
                col = col.push(scene_row);
            }
        } else if mode.as_ref() == "Adaptive" {
            if let Some(adaptive_strength) = get_info(settings, SettingId::AdaptiveNoiseCanceling)
            {
                col = col.push(
                    widget::row![
                        widget::text::body(fl!("adaptive-strength"))
                            .size(13)
                            .width(Length::Fill),
                        crate::theme::accent_text(adaptive_strength, 13.0),
                    ]
                    .align_y(Alignment::Center),
                );
            }
            if let Some(realtime) =
                get_toggle(settings, SettingId::RealTimeAdaptiveNoiseCanceling)
            {
                col = col.push(
                    widget::row![
                        widget::text::body(fl!("realtime-adaptive-anc"))
                            .size(13)
                            .width(Length::Fill),
                        widget::toggler(realtime).on_toggle(|v| {
                            DeviceMessage::SetSetting(
                                SettingId::RealTimeAdaptiveNoiseCanceling,
                                Value::Bool(v),
                            )
                        }),
                    ]
                    .align_y(Alignment::Center),
                );
            }
            if let Some((range, sens)) = get_i32_range(
                settings,
                SettingId::AdaptiveNoiseCancelingSensitivityLevel,
            ) {
                col = col.push(level_selector(
                    fl!("adaptive-sensitivity"),
                    SettingId::AdaptiveNoiseCancelingSensitivityLevel,
                    range,
                    sens,
                ));
            }
        } else if let Some(val) = localized_select_value(select, Some(mode)) {
            col = col.push(secondary_text(val, 13.0));
        }
    } else if let Some((range, custom_level)) = get_i32_range(settings, SettingId::ManualNoiseCanceling) {
        has_any = true;
        col = col.push(level_selector(
            fl!("anc-level"),
            SettingId::ManualNoiseCanceling,
            range,
            custom_level,
        ));
    }

    has_any.then(|| col.into())
}

fn level_selector<'a>(
    label: String,
    setting_id: SettingId,
    range: &openscq30_lib::settings::Range<i32>,
    current: i32,
) -> Element<'a, DeviceMessage> {
    let start = *range.range.start();
    let end = *range.range.end();
    let step = range.step.max(1);
    let mut buttons = widget::row::with_capacity(5).spacing(8);
    let mut v = start;
    while v <= end {
        let selected = v == current;
        buttons = buttons.push(
            widget::button::custom(
                widget::text::body(v.to_string())
                    .size(13)
                    .align_x(iced::alignment::Horizontal::Center)
                    .width(Length::Fill),
            )
            .class(pill_button_style(selected, false))
            .padding([8, 0])
            .width(Length::Fill)
            .on_press(DeviceMessage::SetSetting(setting_id, Value::I32(v))),
        );
        v += step;
    }

    widget::column![
        widget::row![
            secondary_text(label, 12.0).width(Length::Fill),
            crate::theme::accent_text(format!("{current}/{end}"), 12.0),
        ]
        .align_y(Alignment::Center),
        buttons,
    ]
    .spacing(8)
    .into()
}

fn transparency_controls<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    let (select, current) = get_select(settings, SettingId::TransparencyMode)?;
    let mut row = widget::row::with_capacity(select.options.len()).spacing(8);
    for (idx, opt) in select.options.iter().enumerate() {
        let label = select
            .localized_options
            .get(idx)
            .map(String::as_str)
            .unwrap_or(opt.as_ref());
        let selected = opt == current;
        row = row.push(
            widget::button::custom(
                widget::text::body(label)
                    .size(13)
                    .align_x(iced::alignment::Horizontal::Center)
                    .width(Length::Fill),
            )
            .class(pill_button_style(selected, false))
            .padding([8, 12])
            .width(Length::Fill)
            .on_press(DeviceMessage::SetSetting(
                SettingId::TransparencyMode,
                Value::String(opt.clone()),
            )),
        );
    }
    Some(row.into())
}

fn quick_toggles_card<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    let mut rows: Vec<Element<'a, DeviceMessage>> = Vec::new();

    if let Some(touch_lock) = get_toggle(settings, SettingId::TouchLock) {
        rows.push(
            widget::row![
                widget::column![
                    widget::text::body(fl!("touch-lock")).size(15),
                    secondary_text(fl!("touch-lock-subtitle"), 12.0),
                ]
                .spacing(2)
                .width(Length::Fill),
                widget::toggler(touch_lock).on_toggle(|v| {
                    DeviceMessage::SetSetting(SettingId::TouchLock, Value::Bool(v))
                }),
            ]
            .spacing(12)
            .align_y(Alignment::Center)
            .width(Length::Fill)
            .into(),
        );
    }

    if let Some(gaming_mode) = get_toggle(settings, SettingId::GamingMode) {
        rows.push(
            widget::row![
                widget::column![
                    widget::text::body(fl!("gaming-mode")).size(15),
                    secondary_text(fl!("gaming-mode-subtitle"), 12.0),
                ]
                .spacing(2)
                .width(Length::Fill),
                widget::toggler(gaming_mode).on_toggle(|v| {
                    DeviceMessage::SetSetting(SettingId::GamingMode, Value::Bool(v))
                }),
            ]
            .spacing(12)
            .align_y(Alignment::Center)
            .width(Length::Fill)
            .into(),
        );
    }

    if rows.is_empty() {
        return None;
    }

    let mut col = widget::column::with_capacity(rows.len() * 2).spacing(12);
    for (idx, item) in rows.into_iter().enumerate() {
        if idx > 0 {
            col = col.push(soundcore_divider());
        }
        col = col.push(item);
    }
    Some(soundcore_card(col).into())
}

fn has_any_control(settings: &HashMap<SettingId, SettingUiState>) -> bool {
    [
        SettingId::LeftSinglePress,
        SettingId::RightSinglePress,
        SettingId::LeftDoublePress,
        SettingId::RightDoublePress,
        SettingId::LeftTriplePress,
        SettingId::RightTriplePress,
        SettingId::LeftLongPress,
        SettingId::RightLongPress,
        SettingId::SinglePress,
        SettingId::DoublePress,
        SettingId::MfbLongPress,
        SettingId::NormalModeInCycle,
        SettingId::TransparencyModeInCycle,
        SettingId::NoiseCancelingModeInCycle,
    ]
    .iter()
    .any(|id| has_setting(settings, *id))
}
