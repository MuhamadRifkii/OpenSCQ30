use std::collections::HashMap;

use cosmic::{
    Element, Theme,
    iced::{self, Alignment, Color, Length, Point, Rectangle},
    widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl, icons,
    soundcore::{
        SoundcoreMessage, SoundcoreRoute,
        helpers::{
            get_modifiable_select, get_preset_eq, get_select, get_toggle, has_setting,
            localized_select_value,
        },
    },
    theme::{
        SoundcorePalette, centered_page, pill_button_style, radio_indicator, secondary_text,
        soundcore_card, transparent_row_button_style,
    },
};

pub fn view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    preset_expanded: bool,
) -> Element<'a, DeviceMessage> {
    let mut col = widget::column::with_capacity(4).spacing(14);
    col = col.push(subpage_header(fl!("sound-effects")));

    if let Some(spatial) = spatial_audio_card(settings) {
        col = col.push(spatial);
    }

    if let Some(default_eq) = default_presets_card(settings, preset_expanded) {
        col = col.push(default_eq);
    }

    if has_setting(settings, SettingId::VolumeAdjustments)
        || has_setting(settings, SettingId::CustomEqualizerProfile)
    {
        col = col.push(custom_eq_card(settings));
    }

    centered_page(col.padding([8, 0]))
}

pub fn subpage_header<'a>(title: String) -> Element<'a, DeviceMessage> {
    widget::row![
        widget::button::custom(icons::tinted_primary(icons::arrow_back(), 20))
            .class(transparent_row_button_style(18.0))
            .padding([8, 10])
            .on_press(DeviceMessage::Soundcore(SoundcoreMessage::Back)),
        widget::text::title3(title).font(cosmic::font::bold()),
    ]
    .spacing(10)
    .align_y(Alignment::Center)
    .width(Length::Fill)
    .into()
}

fn spatial_audio_card<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
) -> Option<Element<'a, DeviceMessage>> {
    if let Some(spatial) = get_toggle(settings, SettingId::SpatialAudio) {
        let mut col = widget::column::with_capacity(2)
            .push(
                widget::row![
                    icons::tinted_primary(icons::speaker(), 20),
                    widget::text::body(fl!("spatial-audio"))
                        .size(15)
                        .width(Length::Fill),
                    widget::toggler(spatial).on_toggle(|enabled| {
                        DeviceMessage::SetSetting(
                            SettingId::SpatialAudio,
                            Value::Bool(enabled),
                        )
                    }),
                ]
                .spacing(12)
                .align_y(Alignment::Center),
            )
            .spacing(12);

        if spatial
            && let Some((mode_select, current_mode)) =
                get_select(settings, SettingId::SpatialAudioMode)
        {
            let mut pills = widget::row::with_capacity(mode_select.options.len()).spacing(8);
            for (idx, opt) in mode_select.options.iter().enumerate() {
                let label = mode_select
                    .localized_options
                    .get(idx)
                    .map(String::as_str)
                    .unwrap_or(opt.as_ref());
                let selected = opt == current_mode;
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
                        SettingId::SpatialAudioMode,
                        Value::String(opt.clone()),
                    )),
                );
            }
            col = col.push(pills);
        }
        return Some(soundcore_card(col).into());
    }

    if let Some((select, current)) = get_select(settings, SettingId::SurroundSound) {
        let mut pills = widget::row::with_capacity(select.options.len()).spacing(8);
        for (idx, opt) in select.options.iter().enumerate() {
            let label = select
                .localized_options
                .get(idx)
                .map(String::as_str)
                .unwrap_or(opt.as_ref());
            let selected = opt == current;
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
                    SettingId::SurroundSound,
                    Value::String(opt.clone()),
                )),
            );
        }
        return Some(
            soundcore_card(
                widget::column![
                    widget::row![
                        icons::tinted_primary(icons::speaker(), 20),
                        widget::text::title4(fl!("surround-sound"))
                            .font(cosmic::font::semibold()),
                    ]
                    .spacing(10)
                    .align_y(Alignment::Center),
                    pills,
                ]
                .spacing(12),
            )
            .into(),
        );
    }

    None
}

fn default_presets_card<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    preset_expanded: bool,
) -> Option<Element<'a, DeviceMessage>> {
    let (eq_spec, select, presets, current_selected) =
        get_preset_eq(settings, SettingId::PresetEqualizerProfile)?;

    let preview_values = current_selected
        .and_then(|sel| select.options.iter().position(|opt| opt == sel))
        .and_then(|idx| presets.get(idx))
        .cloned()
        .unwrap_or_else(|| {
            crate::soundcore::helpers::active_eq_values(settings).1
        });

    let mut header = widget::column::with_capacity(4)
        .push(
            widget::row![
                icons::tinted_primary(icons::graphic_eq(), 20),
                widget::text::title4(fl!("default-presets"))
                    .font(cosmic::font::semibold()),
            ]
            .spacing(10)
            .align_y(Alignment::Center),
        )
        .push(eq_curve_canvas(
            preview_values,
            eq_spec.min,
            eq_spec.max,
            116.0,
        ))
        .spacing(14);

    let total = select.options.len();
    let max_compact = if !preset_expanded && total > 6 {
        5
    } else {
        total
    };

    #[derive(Clone)]
    enum GridItem {
        Preset(usize),
        MoreToggle,
    }

    let mut items: Vec<GridItem> = (0..max_compact).map(GridItem::Preset).collect();
    if total > 6 {
        items.push(GridItem::MoreToggle);
    }

    let mut grid = widget::column::with_capacity(items.len().div_ceil(2)).spacing(10);
    for chunk in items.chunks(2) {
        let mut row = widget::row::with_capacity(2).spacing(10);
        for item in chunk {
            let btn: Element<'a, DeviceMessage> = match item {
                GridItem::Preset(idx) => {
                    let opt = &select.options[*idx];
                    let label = select
                        .localized_options
                        .get(*idx)
                        .map(String::as_str)
                        .unwrap_or(opt.as_ref());
                    let is_selected = current_selected.is_some_and(|sel| sel == opt);
                    widget::button::custom(
                        widget::text::body(label)
                            .size(13)
                            .align_x(iced::alignment::Horizontal::Center)
                            .width(Length::Fill),
                    )
                    .class(pill_button_style(is_selected, true))
                    .padding([11, 12])
                    .width(Length::Fill)
                    .on_press(DeviceMessage::SetSetting(
                        SettingId::PresetEqualizerProfile,
                        Value::OptionalString(Some(opt.clone())),
                    ))
                    .into()
                }
                GridItem::MoreToggle => {
                    let label = if preset_expanded {
                        fl!("show-less")
                    } else {
                        fl!("more-presets")
                    };
                    widget::button::custom(
                        widget::text::body(label)
                            .size(13)
                            .align_x(iced::alignment::Horizontal::Center)
                            .width(Length::Fill),
                    )
                    .class(pill_button_style(false, false))
                    .padding([11, 12])
                    .width(Length::Fill)
                    .on_press(DeviceMessage::Soundcore(
                        SoundcoreMessage::TogglePresetMore,
                    ))
                    .into()
                }
            };
            row = row.push(btn);
        }
        if chunk.len() == 1 {
            row = row.push(widget::space().width(Length::Fill));
        }
        grid = grid.push(row);
    }

    header = header.push(grid);
    Some(soundcore_card(header).into())
}

fn custom_eq_card<'a>(settings: &'a HashMap<SettingId, SettingUiState>) -> Element<'a, DeviceMessage> {
    let is_custom_active = get_preset_eq(settings, SettingId::PresetEqualizerProfile)
        .is_none_or(|(_, _, _, selected)| selected.is_none());

    let subtitle = get_modifiable_select(settings, SettingId::CustomEqualizerProfile)
        .and_then(|(sel, val)| localized_select_value(sel, val).or_else(|| val.map(|v| v.as_ref())))
        .unwrap_or("Custom")
        .to_string();

    let open_editor_btn = widget::button::custom(
        widget::row![
            icons::tinted_primary(icons::equalizer(), 22),
            widget::column![
                widget::row![
                    widget::text::title4(fl!("custom-eq")).font(cosmic::font::semibold()),
                    icons::chevron_right_tinted(16),
                ]
                .spacing(4)
                .align_y(Alignment::Center),
                secondary_text(subtitle, 12.0),
            ]
            .spacing(2)
            .width(Length::Fill),
        ]
        .spacing(12)
        .align_y(Alignment::Center),
    )
    .class(transparent_row_button_style(12.0))
    .padding([8, 10])
    .width(Length::Fill)
    .on_press(DeviceMessage::Soundcore(SoundcoreMessage::Navigate(
        SoundcoreRoute::CustomEqualizer,
    )));

    let radio_btn = widget::button::custom(radio_indicator(is_custom_active))
        .class(transparent_row_button_style(18.0))
        .padding([10, 10])
        .on_press(DeviceMessage::Soundcore(SoundcoreMessage::ActivateCustomEq));

    soundcore_card(
        widget::row![open_editor_btn, radio_btn]
            .spacing(8)
            .align_y(Alignment::Center)
            .width(Length::Fill),
    )
    .into()
}

pub fn eq_curve_canvas<'a, M: 'a>(
    bands: Vec<i16>,
    min: i16,
    max: i16,
    height: f32,
) -> Element<'a, M> {
    widget::canvas(EqualizerCurveCanvas { bands, min, max })
        .width(Length::Fill)
        .height(height)
        .into()
}

struct EqualizerCurveCanvas {
    bands: Vec<i16>,
    min: i16,
    max: i16,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for EqualizerCurveCanvas {
    type State = ();

    fn draw(
        &self,
        _state: &Self::State,
        renderer: &cosmic::Renderer,
        theme: &Theme,
        bounds: Rectangle,
        _cursor: iced::core::mouse::Cursor,
    ) -> Vec<widget::canvas::Geometry<cosmic::Renderer>> {
        let p = SoundcorePalette::from_theme(theme);
        let mut frame = widget::canvas::Frame::new(renderer, bounds.size());
        let w = bounds.width;
        let h = bounds.height;
        let pad_x = 12.0;
        let pad_y = 14.0;
        let chart_w = (w - pad_x * 2.0).max(1.0);
        let chart_h = (h - pad_y * 2.0).max(1.0);

        let grid_color = Color {
            a: if p.is_dark { 0.08 } else { 0.1 },
            ..p.text_primary
        };

        // Vertical grid lines
        for i in 0..=4 {
            let x = pad_x + chart_w * (i as f32 / 4.0);
            frame.stroke(
                &widget::canvas::Path::line(
                    Point::new(x, pad_y),
                    Point::new(x, pad_y + chart_h),
                ),
                widget::canvas::Stroke::default()
                    .with_color(grid_color)
                    .with_width(1.0),
            );
        }

        // Baseline at 0 dB
        let min_f = f32::from(self.min);
        let max_f = f32::from(self.max).max(min_f + 1.0);
        let zero_norm = ((0.0 - min_f) / (max_f - min_f)).clamp(0.0, 1.0);
        let zero_y = pad_y + chart_h * (1.0 - zero_norm);
        frame.stroke(
            &widget::canvas::Path::line(
                Point::new(pad_x, zero_y),
                Point::new(pad_x + chart_w, zero_y),
            ),
            widget::canvas::Stroke::default()
                .with_color(Color { a: 0.16, ..p.text_secondary })
                .with_width(1.0),
        );

        let points: Vec<Point> = if self.bands.is_empty() {
            vec![
                Point::new(pad_x, zero_y),
                Point::new(pad_x + chart_w, zero_y),
            ]
        } else if self.bands.len() == 1 {
            let norm = ((f32::from(self.bands[0]) - min_f) / (max_f - min_f)).clamp(0.0, 1.0);
            let y = pad_y + chart_h * (1.0 - norm);
            vec![Point::new(pad_x, y), Point::new(pad_x + chart_w, y)]
        } else {
            let last_idx = (self.bands.len() - 1) as f32;
            self.bands
                .iter()
                .enumerate()
                .map(|(i, val)| {
                    let x = pad_x + chart_w * (i as f32 / last_idx);
                    let norm = ((f32::from(*val) - min_f) / (max_f - min_f)).clamp(0.0, 1.0);
                    let y = pad_y + chart_h * (1.0 - norm);
                    Point::new(x, y)
                })
                .collect()
        };

        let area_path = widget::canvas::Path::new(|b| {
            b.move_to(Point::new(points[0].x, pad_y + chart_h));
            b.line_to(points[0]);
            for pair in points.windows(2) {
                let p0 = pair[0];
                let p1 = pair[1];
                let mid_x = (p0.x + p1.x) * 0.5;
                b.bezier_curve_to(Point::new(mid_x, p0.y), Point::new(mid_x, p1.y), p1);
            }
            if let Some(last) = points.last() {
                b.line_to(Point::new(last.x, pad_y + chart_h));
            }
            b.close();
        });

        let area_grad = widget::canvas::gradient::Linear::new(
            Point::new(0.0, pad_y),
            Point::new(0.0, pad_y + chart_h),
        )
        .add_stop(0.0, Color { a: 0.24, ..p.accent })
        .add_stop(1.0, Color { a: 0.0, ..p.accent });
        frame.fill(
            &area_path,
            widget::canvas::gradient::Gradient::Linear(area_grad),
        );

        let stroke_path = widget::canvas::Path::new(|b| {
            b.move_to(points[0]);
            for pair in points.windows(2) {
                let p0 = pair[0];
                let p1 = pair[1];
                let mid_x = (p0.x + p1.x) * 0.5;
                b.bezier_curve_to(Point::new(mid_x, p0.y), Point::new(mid_x, p1.y), p1);
            }
        });

        let line_grad = widget::canvas::gradient::Linear::new(
            Point::new(pad_x, 0.0),
            Point::new(pad_x + chart_w, 0.0),
        )
        .add_stop(0.0, p.gradient_pink)
        .add_stop(1.0, p.gradient_cyan);
        let mut stroke = widget::canvas::Stroke::default().with_width(2.6);
        stroke.style = widget::canvas::Style::Gradient(
            widget::canvas::gradient::Gradient::Linear(line_grad),
        );
        stroke.line_cap = widget::canvas::LineCap::Round;
        stroke.line_join = widget::canvas::LineJoin::Round;
        frame.stroke(&stroke_path, stroke);

        vec![frame.into_geometry()]
    }
}
