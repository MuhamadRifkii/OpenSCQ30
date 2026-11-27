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
        SoundcoreMessage, battery,
        helpers::{get_optional_select, get_select, get_toggle, has_setting, localized_select_value},
        sound_effects::subpage_header,
    },
    theme::{
        SoundcorePalette, centered_page, pill_button_style, secondary_text, soundcore_card,
        soundcore_divider, soundcore_grouped_card, transparent_row_button_style,
    },
};

pub fn view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    picking_gesture: Option<SettingId>,
) -> Element<'a, DeviceMessage> {
    if let Some(target_id) = picking_gesture {
        return picker_view(settings, target_id);
    }

    let mut col = widget::column::with_capacity(6).spacing(14);
    col = col.push(
        widget::row![
            widget::container(subpage_header(fl!("controls"))).width(Length::Fill),
            has_setting(settings, SettingId::ResetButtonsToDefault).then(|| {
                widget::button::custom(widget::text::body(fl!("reset")).size(13))
                    .class(pill_button_style(false, false))
                    .padding([8, 14])
                    .on_press(DeviceMessage::Soundcore(SoundcoreMessage::AskResetControls))
            }),
        ]
        .align_y(Alignment::Center)
        .width(Length::Fill),
    );

    if let Some((left_col, right_col)) = build_earbud_gesture_columns(settings) {
        col = col.push(
            widget::row![
                soundcore_grouped_card(left_col).width(Length::Fill),
                soundcore_grouped_card(right_col).width(Length::Fill),
            ]
            .spacing(12)
            .width(Length::Fill),
        );
    }

    let single_side_ids = [
        SettingId::SinglePress,
        SettingId::DoublePress,
        SettingId::MfbLongPress,
    ];
    let single_rows: Vec<(SettingId, String, &SettingUiState)> = single_side_ids
        .into_iter()
        .filter_map(|id| {
            let ui = settings.get(&id)?;
            Some((id, ui.translated_name.clone(), ui))
        })
        .collect();
    if !single_rows.is_empty() {
        let mut s_col = widget::column::with_capacity(single_rows.len() * 2);
        for (idx, (id, title, _)) in single_rows.iter().enumerate() {
            if idx > 0 {
                s_col = s_col.push(soundcore_divider());
            }
            let val_text = gesture_current_label(settings, *id).unwrap_or_else(|| fl!("none"));
            s_col = s_col.push(gesture_row(title.clone(), val_text, *id));
        }
        col = col.push(soundcore_grouped_card(s_col));
    }

    if let Some(cycle_card) = sound_mode_cycle_card(settings) {
        col = col.push(cycle_card);
    }

    centered_page(col.padding([8, 0]))
}

fn build_earbud_gesture_columns<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<(Element<'a, DeviceMessage>, Element<'a, DeviceMessage>)> {
    let pairs = [
        (SettingId::LeftSinglePress, SettingId::RightSinglePress),
        (SettingId::LeftDoublePress, SettingId::RightDoublePress),
        (SettingId::LeftTriplePress, SettingId::RightTriplePress),
        (SettingId::LeftLongPress, SettingId::RightLongPress),
        (
            SettingId::LeftDoublePressDuringCall,
            SettingId::RightDoublePressDuringCall,
        ),
        (
            SettingId::LeftLongPressDuringCall,
            SettingId::RightLongPressDuringCall,
        ),
    ];

    let mut left_items = Vec::new();
    let mut right_items = Vec::new();

    for (l_id, r_id) in pairs {
        if has_setting(settings, l_id) {
            left_items.push(l_id);
        }
        if has_setting(settings, r_id) {
            right_items.push(r_id);
        }
    }

    if left_items.is_empty() && right_items.is_empty() {
        return None;
    }

    let left_col = earbud_column("L", settings, &left_items);
    let right_col = earbud_column("R", settings, &right_items);
    Some((left_col, right_col))
}

fn earbud_column<'a>(
    side_label: &'static str,
    settings: &'a HashMap<SettingId, SettingUiState>,
    ids: &[SettingId],
) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(ids.len() * 2 + 2)
        .push(
            widget::container(
                widget::row![
                    battery::ear_badge(side_label, 20),
                    widget::text::title4(if side_label == "L" {
                        fl!("left")
                    } else {
                        fl!("right")
                    })
                    .font(cosmic::font::semibold()),
                ]
                .spacing(8)
                .align_y(Alignment::Center),
            )
            .padding([14, 16]),
        );

    for id in ids {
        let title = settings
            .get(id)
            .map(|s| s.translated_name.clone())
            .unwrap_or_default();
        let val = gesture_current_label(settings, *id).unwrap_or_else(|| fl!("none"));
        col = col.push(soundcore_divider()).push(gesture_row(title, val, *id));
    }

    col.into()
}

fn gesture_row<'a>(
    title: String,
    value_label: String,
    id: SettingId,
) -> Element<'a, DeviceMessage> {
    widget::button::custom(
        widget::row![
            widget::column![
                widget::text::body(title).size(14),
                crate::theme::accent_text(value_label, 12.0),
            ]
            .spacing(3)
            .width(Length::Fill),
            icons::chevron_right_tinted(16),
        ]
        .spacing(8)
        .align_y(Alignment::Center)
        .width(Length::Fill),
    )
    .class(transparent_row_button_style(12.0))
    .padding([12, 16])
    .width(Length::Fill)
    .on_press(DeviceMessage::Soundcore(
        SoundcoreMessage::OpenGesturePicker(id),
    ))
    .into()
}

fn gesture_current_label(
    settings: &HashMap<SettingId, SettingUiState>,
    id: SettingId,
) -> Option<String> {
    if let Some((select, value)) = get_optional_select(settings, id) {
        return Some(
            localized_select_value(select, value)
                .map(str::to_string)
                .unwrap_or_else(|| fl!("none")),
        );
    }
    if let Some((select, value)) = get_select(settings, id) {
        return localized_select_value(select, Some(value)).map(str::to_string);
    }
    None
}

fn sound_mode_cycle_card<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    let items = cycle_items(settings);
    if items.is_empty() {
        return None;
    }

    let mut col = widget::column::with_capacity(items.len() + 2)
        .push(widget::text::title4(fl!("ambient-sound-cycle")).font(cosmic::font::semibold()))
        .push(secondary_text(fl!("ambient-sound-cycle-description"), 12.0))
        .spacing(10);

    for (id, label, checked) in items {
        col = col.push(
            widget::checkbox(checked)
                .label(label)
                .on_toggle(move |v| DeviceMessage::SetSetting(id, Value::Bool(v))),
        );
    }

    Some(soundcore_card(col).into())
}

fn cycle_items(settings: &HashMap<SettingId, SettingUiState>) -> Vec<(SettingId, String, bool)> {
    [
        SettingId::NoiseCancelingModeInCycle,
        SettingId::NormalModeInCycle,
        SettingId::TransparencyModeInCycle,
    ]
    .into_iter()
    .filter_map(|id| {
        let checked = get_toggle(settings, id)?;
        let label = settings
            .get(&id)
            .map(|s| s.translated_name.clone())
            .unwrap_or_else(|| format!("{id:?}"));
        Some((id, label, checked))
    })
    .collect()
}

fn picker_view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    target_id: SettingId,
) -> Element<'a, DeviceMessage> {
    let title = settings
        .get(&target_id)
        .map(|s| s.translated_name.clone())
        .unwrap_or_else(|| format!("{target_id:?}"));

    let (select, current_opt, is_optional): (&openscq30_lib::settings::Select, Option<&Cow<'static, str>>, bool) =
        if let Some((sel, val)) = get_optional_select(settings, target_id) {
            (sel, val, true)
        } else if let Some((sel, val)) = get_select(settings, target_id) {
            (sel, Some(val), false)
        } else {
            return centered_page(
                widget::column![
                    picker_header(title),
                    soundcore_card(secondary_text(fl!("not-available"), 13.0)),
                ]
                .spacing(14),
            );
        };

    let cycle = cycle_items(settings);
    let mut list_col = widget::column::with_capacity(select.options.len() * 2 + 2);

    for (idx, opt) in select.options.iter().enumerate() {
        if idx > 0 {
            list_col = list_col.push(soundcore_divider());
        }
        let label = select
            .localized_options
            .get(idx)
            .map(String::as_str)
            .unwrap_or(opt.as_ref());
        let selected = current_opt.is_some_and(|v| v == opt);
        let is_ambient = opt.as_ref() == "AmbientSoundMode";

        let value = if is_optional {
            Value::OptionalString(Some(opt.clone()))
        } else {
            Value::String(opt.clone())
        };

        let mut row_col = widget::column::with_capacity(2).spacing(8);
        row_col = row_col.push(
            widget::button::custom(
                widget::row![
                    widget::column![
                        widget::text::body(label).size(15).font(if selected {
                            cosmic::font::semibold()
                        } else {
                            cosmic::font::default()
                        }),
                    ]
                    .width(Length::Fill),
                    selected.then(|| icons::tinted_accent(icons::check(), 18)),
                ]
                .align_y(Alignment::Center)
                .width(Length::Fill),
            )
            .class(transparent_row_button_style(12.0))
            .padding([12, 16])
            .width(Length::Fill)
            .on_press(DeviceMessage::SetSetting(target_id, value)),
        );

        if is_ambient && !cycle.is_empty() {
            let mut nested = widget::column::with_capacity(cycle.len() + 1)
                .push(secondary_text(fl!("ambient-sound-cycle-description"), 12.0))
                .spacing(8);

            for (cycle_id, cycle_label, checked) in &cycle {
                let c_id = *cycle_id;
                nested = nested.push(
                    widget::checkbox(*checked)
                        .label(cycle_label.clone())
                        .on_toggle(move |v| DeviceMessage::SetSetting(c_id, Value::Bool(v))),
                );
            }

            row_col = row_col.push(
                widget::container(nested)
                    .padding([10, 16])
                    .width(Length::Fill)
                    .class(theme::Container::custom(|theme| {
                        let p = SoundcorePalette::from_theme(theme);
                        widget::container::Style {
                            background: Some(Background::Color(Color {
                                a: 0.45,
                                ..p.surface_elevated
                            })),
                            border: Border {
                                radius: 12.0.into(),
                                ..Default::default()
                            },
                            ..Default::default()
                        }
                    })),
            );
        }

        list_col = list_col.push(row_col);
    }

    if is_optional {
        let selected = current_opt.is_none();
        list_col = list_col.push(soundcore_divider()).push(
            widget::button::custom(
                widget::row![
                    widget::text::body(fl!("none"))
                        .size(15)
                        .width(Length::Fill),
                    selected.then(|| icons::tinted_accent(icons::check(), 18)),
                ]
                .align_y(Alignment::Center)
                .width(Length::Fill),
            )
            .class(transparent_row_button_style(12.0))
            .padding([12, 16])
            .width(Length::Fill)
            .on_press(DeviceMessage::SetSetting(
                target_id,
                Value::OptionalString(None),
            )),
        );
    }

    let _ = iced::alignment::Horizontal::Center;
    centered_page(
        widget::column![picker_header(title), soundcore_grouped_card(list_col)]
            .spacing(14)
            .padding([8, 0]),
    )
}

fn picker_header<'a>(title: String) -> Element<'a, DeviceMessage> {
    widget::row![
        widget::button::custom(icons::tinted_primary(icons::arrow_back(), 20))
            .class(transparent_row_button_style(18.0))
            .padding([8, 10])
            .on_press(DeviceMessage::Soundcore(
                SoundcoreMessage::CloseGesturePicker,
            )),
        widget::text::title3(title).font(cosmic::font::bold()),
    ]
    .spacing(10)
    .align_y(Alignment::Center)
    .width(Length::Fill)
    .into()
}
