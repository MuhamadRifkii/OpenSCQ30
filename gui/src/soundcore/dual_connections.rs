use std::{borrow::Cow, collections::HashMap};

use cosmic::{
    Element,
    iced::{Alignment, Background, Border, Color, Length},
    theme, widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl, icons,
    soundcore::{
        SoundcoreMessage,
        helpers::{get_multi_select_with_remove, get_toggle, has_setting},
        sound_effects::subpage_header,
    },
    theme::{
        SOUNDCORE_CORAL, SoundcorePalette, accent_cta_button_style, accent_text, centered_page,
        secondary_text, soundcore_card, soundcore_divider, soundcore_grouped_card,
        transparent_row_button_style,
    },
};

pub fn view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    manage_history: bool,
    pairing_notice: bool,
) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(7).spacing(14);
    col = col.push(subpage_header(fl!("dual-connections")));

    let dual_enabled = get_toggle(settings, SettingId::DualConnections).unwrap_or(false);
    col = col.push(soundcore_card(
        widget::column![
            widget::row![
                widget::text::title4(fl!("dual-connections"))
                    .font(cosmic::font::semibold())
                    .width(Length::Fill),
                widget::toggler(dual_enabled).on_toggle(|enabled| {
                    DeviceMessage::SetSetting(
                        SettingId::DualConnections,
                        Value::Bool(enabled),
                    )
                }),
            ]
            .spacing(12)
            .align_y(Alignment::Center),
            secondary_text(fl!("dual-connections-description"), 13),
        ]
        .spacing(8),
    ));

    if let Some((select, active_macs)) =
        get_multi_select_with_remove(settings, SettingId::DualConnectionsDevices)
    {
        let mut current_devices: Vec<(Cow<'static, str>, String)> = Vec::new();
        let mut history_devices: Vec<(Cow<'static, str>, String)> = Vec::new();

        for (idx, mac) in select.options.iter().enumerate() {
            let name = select
                .localized_options
                .get(idx)
                .cloned()
                .unwrap_or_else(|| mac.to_string());
            if active_macs.iter().any(|active| active == mac) {
                current_devices.push((mac.clone(), name));
            } else {
                history_devices.push((mac.clone(), name));
            }
        }

        if !current_devices.is_empty() {
            col = col.push(
                widget::container(secondary_text(fl!("current-connected-devices"), 13))
                    .padding([4, 4]),
            );
            let mut cur_col = widget::column::with_capacity(current_devices.len() * 2);
            for (idx, (mac, name)) in current_devices.iter().enumerate() {
                if idx > 0 {
                    cur_col = cur_col.push(soundcore_divider());
                }
                let next_values: Vec<Cow<'static, str>> = active_macs
                    .iter()
                    .filter(|m| *m != mac)
                    .cloned()
                    .collect();

                cur_col = cur_col.push(
                    widget::container(
                        widget::row![
                            icons::tinted_accent(icons::graphic_eq(), 18),
                            widget::text::body(name.clone())
                                .size(15)
                                .width(Length::Fill),
                            widget::toggler(true).on_toggle(move |_| {
                                DeviceMessage::SetSetting(
                                    SettingId::DualConnectionsDevices,
                                    Value::StringVec(next_values.clone()),
                                )
                            }),
                        ]
                        .spacing(12)
                        .align_y(Alignment::Center)
                        .width(Length::Fill),
                    )
                    .padding([12, 16]),
                );
            }
            col = col.push(soundcore_grouped_card(cur_col));
        }

        if !history_devices.is_empty() {
            let manage_label = if manage_history {
                fl!("done")
            } else {
                fl!("manage")
            };
            col = col.push(
                widget::row![
                    secondary_text(fl!("history"), 13).width(Length::Fill),
                    widget::button::custom(accent_text(manage_label, 13))
                        .class(transparent_row_button_style(10.0))
                        .padding([4, 8])
                        .on_press(DeviceMessage::Soundcore(
                            SoundcoreMessage::ToggleManageDualHistory,
                        )),
                ]
                .align_y(Alignment::Center)
                .padding([4, 4]),
            );

            let mut hist_col = widget::column::with_capacity(history_devices.len() * 2);
            for (idx, (mac, name)) in history_devices.iter().enumerate() {
                if idx > 0 {
                    hist_col = hist_col.push(soundcore_divider());
                }
                let mac_owned = mac.to_string();
                let name_owned = name.clone();
                let mut next_values = active_macs.to_vec();
                next_values.push(mac.clone());

                let trailing: Element<'a, DeviceMessage> = if manage_history {
                    widget::button::custom(
                        widget::text::body(fl!("remove"))
                            .size(13)
                            .class(theme::Text::Custom(|_| cosmic::iced::widget::text::Style {
                                color: Some(SOUNDCORE_CORAL),
                            })),
                    )
                    .class(transparent_row_button_style(10.0))
                    .padding([6, 10])
                    .on_press(DeviceMessage::Soundcore(
                        SoundcoreMessage::AskRemoveDualDevice(mac_owned, name_owned),
                    ))
                    .into()
                } else {
                    widget::toggler(false)
                        .on_toggle(move |_| {
                            DeviceMessage::SetSetting(
                                SettingId::DualConnectionsDevices,
                                Value::StringVec(next_values.clone()),
                            )
                        })
                        .into()
                };

                hist_col = hist_col.push(
                    widget::container(
                        widget::row![
                            widget::text::body(name.clone())
                                .size(15)
                                .width(Length::Fill),
                            trailing,
                        ]
                        .spacing(12)
                        .align_y(Alignment::Center)
                        .width(Length::Fill),
                    )
                    .padding([12, 16]),
                );
            }
            col = col.push(soundcore_grouped_card(hist_col));
        }
    }

    if pairing_notice {
        col = col.push(
            widget::container(
                widget::row![
                    icons::tinted_accent(icons::bluetooth(), 18),
                    widget::text::body(fl!("pairing-mode-notice")).size(13),
                ]
                .spacing(10)
                .align_y(Alignment::Center),
            )
            .padding([12, 14])
            .width(Length::Fill)
            .class(theme::Container::custom(|theme| {
                let p = SoundcorePalette::from_theme(theme);
                widget::container::Style {
                    background: Some(Background::Color(Color { a: 0.14, ..p.accent })),
                    border: Border {
                        color: Color { a: 0.35, ..p.accent },
                        width: 1.0,
                        radius: 14.0.into(),
                    },
                    text_color: Some(p.text_primary),
                    ..Default::default()
                }
            })),
        );
    }

    if has_setting(settings, SettingId::DualConnectionsConnectNewDevice) {
        col = col.push(
            widget::button::custom(
                widget::row![
                    icons::tinted_on_accent(icons::bluetooth(), 18),
                    widget::text::body(fl!("connect-new-device"))
                        .size(15)
                        .font(cosmic::font::semibold()),
                ]
                .spacing(8)
                .align_y(Alignment::Center),
            )
            .class(accent_cta_button_style())
            .padding([13, 20])
            .width(Length::Fill)
            .on_press(DeviceMessage::Soundcore(
                SoundcoreMessage::RequestConnectNewDevice,
            )),
        );
    }

    centered_page(col.padding([8, 0]))
}
