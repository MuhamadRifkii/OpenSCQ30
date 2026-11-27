use std::collections::HashMap;

use cosmic::{
    Element,
    iced::{self, Alignment, Background, Border, Color, Length},
    theme, widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState, controls},
    fl, icons,
    soundcore::{
        SoundcoreMessage,
        helpers::{
            get_equalizer, get_import_string, get_info, get_modifiable_select, get_preset_eq,
            has_setting,
        },
        sound_effects::{eq_curve_canvas, subpage_header},
    },
    theme::{
        SoundcorePalette, accent_cta_button_style, centered_page, pill_button_style,
        secondary_text, soundcore_card,
    },
};

pub fn view<'a>(settings: &'a HashMap<SettingId, SettingUiState>) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(6).spacing(14);
    col = col.push(subpage_header(fl!("custom-eq")));

    let is_custom_active = get_preset_eq(settings, SettingId::PresetEqualizerProfile)
        .is_none_or(|(_, _, _, selected)| selected.is_none());

    if !is_custom_active {
        let banner = soundcore_card(
            widget::row![
                widget::column![
                    widget::text::body(fl!("custom-eq-inactive")).size(14),
                    secondary_text(fl!("custom-eq-inactive-hint"), 12.0),
                ]
                .spacing(2)
                .width(Length::Fill),
                widget::button::custom(
                    widget::text::body(fl!("activate"))
                        .size(13)
                        .font(cosmic::font::semibold()),
                )
                .class(accent_cta_button_style())
                .padding([8, 16])
                .on_press(DeviceMessage::Soundcore(SoundcoreMessage::ActivateCustomEq)),
            ]
            .spacing(12)
            .align_y(Alignment::Center),
        );
        col = col.push(banner);
    } else {
        col = col.push(
            widget::container(
                widget::row![
                    icons::tinted_accent(icons::check(), 16),
                    crate::theme::accent_text(fl!("custom-eq-active"), 13.0),
                ]
                .spacing(6)
                .align_y(Alignment::Center),
            )
            .padding([6, 12])
            .class(theme::Container::custom(|theme| {
                let p = SoundcorePalette::from_theme(theme);
                widget::container::Style {
                    background: Some(Background::Color(Color { a: 0.14, ..p.accent })),
                    border: Border {
                        color: Color { a: 0.35, ..p.accent },
                        width: 1.0,
                        radius: 12.0.into(),
                    },
                    ..Default::default()
                }
            })),
        );
    }

    if let Some((select, selected_value)) =
        get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
    {
        let selected_idx = selected_value
            .and_then(|val| select.options.iter().position(|opt| opt == val));
        let has_selected = selected_value.is_some();

        let dropdown = widget::dropdown(
            &select.localized_options,
            selected_idx,
            |idx| DeviceMessage::Soundcore(SoundcoreMessage::SelectCustomEqProfile(idx)),
        )
        .width(Length::Fill);

        let add_btn = widget::button::custom(icons::tinted_primary(icons::add(), 18))
            .class(pill_button_style(false, false))
            .padding([8, 10])
            .on_press(DeviceMessage::ShowModifiableSelectAddDialog(
                SettingId::CustomEqualizerProfile,
            ));

        let mut remove_btn =
            widget::button::custom(icons::tinted_primary(icons::remove(), 18))
                .class(pill_button_style(false, false))
                .padding([8, 10]);
        if has_selected {
            remove_btn = remove_btn.on_press(DeviceMessage::ShowModifiableSelectRemoveDialog(
                SettingId::CustomEqualizerProfile,
            ));
        }

        col = col.push(soundcore_card(
            widget::column![
                widget::text::body(fl!("saved-custom-profiles")).size(13),
                widget::row![dropdown, add_btn, remove_btn]
                    .spacing(8)
                    .align_y(Alignment::Center),
            ]
            .spacing(10),
        ));
    }

    if let Some((eq_spec, read_only, values)) =
        get_equalizer(settings, SettingId::VolumeAdjustments)
    {
        col = col.push(soundcore_card(eq_curve_canvas(
            values.to_vec(),
            eq_spec.min,
            eq_spec.max,
            136.0,
        )));

        if read_only {
            let mut ro_col = widget::column::with_capacity(eq_spec.band_hz.len()).spacing(8);
            for (i, hz) in eq_spec.band_hz.iter().copied().enumerate() {
                let raw = values.get(i).copied().unwrap_or(0);
                let divisor = 10i16.pow(eq_spec.fraction_digits as u32);
                let sign = if raw < 0 { "-" } else { "" };
                let int_part = (raw / divisor).abs();
                let frac_part = (raw % divisor).abs();
                ro_col = ro_col.push(
                    widget::row![
                        widget::text::body(fl!("hz", hz = hz)).width(Length::Fill),
                        secondary_text(format!("{sign}{int_part}.{frac_part} dB"), 13.0),
                    ]
                    .align_y(Alignment::Center),
                );
            }
            col = col.push(soundcore_card(ro_col));
        } else {
            let bands = controls::horizontal_equalizer(eq_spec, values, |band, val| {
                DeviceMessage::SetEqualizerBand(SettingId::VolumeAdjustments, band, val)
            });
            col = col.push(soundcore_card(widget::column::with_children(bands).spacing(10)));
        }
    }

    if let Some(export_json) = get_info(settings, SettingId::ExportCustomEqualizerProfilesOutput) {
        let json_owned = export_json.to_string();
        col = col.push(soundcore_card(
            widget::column![
                widget::row![
                    widget::text::body(fl!("export-custom-equalizer-profiles"))
                        .size(14)
                        .width(Length::Fill),
                    widget::button::custom(
                        widget::row![
                            icons::tinted_primary(icons::copy(), 16),
                            widget::text::body(fl!("copy")).size(12),
                        ]
                        .spacing(6)
                        .align_y(Alignment::Center),
                    )
                    .class(pill_button_style(false, false))
                    .padding([6, 12])
                    .on_press(DeviceMessage::CopyToClipboard(json_owned)),
                ]
                .align_y(Alignment::Center),
                secondary_text(export_json, 12.0),
            ]
            .spacing(8),
        ));
    } else if has_setting(settings, SettingId::ExportCustomEqualizerProfiles) {
        col = col.push(soundcore_card(
            widget::row![
                widget::text::body(fl!("export-custom-equalizer-profiles"))
                    .size(14)
                    .width(Length::Fill),
                widget::button::custom(widget::text::body(fl!("export")).size(13))
                    .class(pill_button_style(false, false))
                    .padding([8, 14])
                    .on_press(DeviceMessage::SetSetting(
                        SettingId::ExportCustomEqualizerProfiles,
                        Value::Bool(false),
                    )),
            ]
            .align_y(Alignment::Center),
        ));
    }

    if let Some((import_text, _)) =
        get_import_string(settings, SettingId::ImportCustomEqualizerProfiles)
    {
        let text_owned = import_text.to_string();
        col = col.push(soundcore_card(
            widget::column![
                widget::text::body(fl!("import-custom-equalizer-profiles")).size(14),
                widget::row![
                    widget::text_input("JSON", import_text)
                        .on_input(|s| {
                            DeviceMessage::SetImportString(
                                SettingId::ImportCustomEqualizerProfiles,
                                s,
                            )
                        })
                        .width(Length::Fill),
                    widget::button::custom(widget::text::body(fl!("import")).size(13))
                        .class(pill_button_style(false, false))
                        .padding([8, 14])
                        .on_press(DeviceMessage::AskConfirmImportString(
                            SettingId::ImportCustomEqualizerProfiles,
                            text_owned,
                        )),
                ]
                .spacing(8)
                .align_y(Alignment::Center),
            ]
            .spacing(10),
        ));
    }

    let _ = iced::alignment::Horizontal::Center;
    centered_page(col.padding([8, 0]))
}
