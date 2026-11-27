use std::collections::HashMap;

use cosmic::{
    Element, Theme,
    iced::{
        self, Alignment, Background, Border, Color, Length, Point, Rectangle,
        alignment::Vertical,
        core::text::Alignment as TextAlignment,
    },
    theme, widget,
};
use openscq30_lib::settings::{SettingId, Value};

use crate::{
    device_settings::{Message as DeviceMessage, SettingUiState},
    fl, icons,
    soundcore::{
        SoundcoreMessage,
        helpers::{get_i32_range, get_info, get_select, get_toggle, parse_db_value},
        sound_effects::subpage_header,
    },
    theme::{
        SOUNDCORE_AMBER, SOUNDCORE_BLUE, SOUNDCORE_CORAL, SoundcorePalette, centered_page,
        pill_button_style, secondary_text, soundcore_card, transparent_row_button_style,
    },
};

#[derive(Clone, Copy, PartialEq, Eq)]
enum VolumeStatusLevel {
    Normal,
    Acceptable,
    High,
}

pub fn view<'a>(
    settings: &'a HashMap<SettingId, SettingUiState>,
    displayed_db: f32,
    wave_phase: f32,
    show_info: bool,
) -> Element<'a, DeviceMessage> {
    let limit_enabled = get_toggle(settings, SettingId::LimitHighVolume).unwrap_or(false);
    let (limit_range, current_limit) = get_i32_range(settings, SettingId::LimitHighVolumeDbLimit)
        .map_or((75..=100, 90), |(r, v)| (r.range.clone(), v));
    let raw_db = get_info(settings, SettingId::LimitHighVolumeCurrentDb)
        .and_then(parse_db_value);
    let shown_db = if displayed_db <= 0.01 {
        raw_db.unwrap_or(0) as f32
    } else {
        displayed_db
    };
    let display_db_int = shown_db.round().clamp(0.0, 120.0) as i32;

    let status_level = if display_db_int >= current_limit {
        VolumeStatusLevel::High
    } else if display_db_int >= 70 {
        VolumeStatusLevel::Acceptable
    } else {
        VolumeStatusLevel::Normal
    };

    let mut col = widget::column::with_capacity(6).spacing(14);
    col = col.push(
        widget::row![
            widget::container(subpage_header(fl!("safe-volume"))).width(Length::Fill),
            widget::button::custom(icons::tinted_secondary(icons::info(), 20))
                .class(transparent_row_button_style(18.0))
                .padding([8, 10])
                .on_press(DeviceMessage::Soundcore(
                    SoundcoreMessage::ToggleSafeVolumeInfo,
                )),
        ]
        .align_y(Alignment::Center)
        .width(Length::Fill),
    );

    if show_info {
        col = col.push(
            widget::container(secondary_text(fl!("safe-volume-info"), 12.0))
                .padding([12, 14])
                .width(Length::Fill)
                .class(theme::Container::custom(|theme| {
                    let p = SoundcorePalette::from_theme(theme);
                    widget::container::Style {
                        background: Some(Background::Color(p.surface_elevated)),
                        border: Border {
                            color: p.divider,
                            width: 1.0,
                            radius: 14.0.into(),
                        },
                        ..Default::default()
                    }
                })),
        );
    }

    let (status_text, status_color) = match status_level {
        VolumeStatusLevel::Normal => (fl!("volume-status-normal"), SoundcorePalette::from_color_cyan()),
        VolumeStatusLevel::Acceptable => (fl!("volume-status-acceptable"), SOUNDCORE_AMBER),
        VolumeStatusLevel::High => (fl!("volume-status-high"), SOUNDCORE_CORAL),
    };

    let meter_card = soundcore_card(
        widget::column![
            widget::text::body(fl!("current-headphone-volume")).size(14),
            widget::row![
                widget::canvas(SmileyFaceCanvas {
                    color: status_color,
                    level: status_level,
                })
                .width(26)
                .height(26),
                widget::text::title3(status_text)
                    .font(cosmic::font::bold())
                    .size(18),
            ]
            .spacing(10)
            .align_y(Alignment::Center),
            widget::container(
                widget::canvas(DecibelMeterCanvas {
                    current_db: shown_db,
                    display_db_int,
                    limit_db: current_limit,
                    wave_phase,
                    status_level,
                    has_reading: raw_db.is_some(),
                })
                .width(Length::Fill)
                .height(250.0),
            )
            .width(Length::Fill)
            .align_x(iced::alignment::Horizontal::Center),
        ]
        .spacing(12)
        .align_x(Alignment::Center)
        .width(Length::Fill),
    );
    col = col.push(meter_card);

    if let Some((refresh_select, current_rate)) =
        get_select(settings, SettingId::LimitHighVolumeRefreshRate)
    {
        let mut pills = widget::row::with_capacity(refresh_select.options.len()).spacing(8);
        for (idx, opt) in refresh_select.options.iter().enumerate() {
            let label = refresh_select
                .localized_options
                .get(idx)
                .map(String::as_str)
                .unwrap_or(opt.as_ref());
            let selected = opt == current_rate;
            pills = pills.push(
                widget::button::custom(
                    widget::text::body(label)
                        .size(13)
                        .align_x(iced::alignment::Horizontal::Center)
                        .width(Length::Fill),
                )
                .class(pill_button_style(selected, false))
                .padding([10, 12])
                .width(Length::Fill)
                .on_press(DeviceMessage::SetSetting(
                    SettingId::LimitHighVolumeRefreshRate,
                    Value::String(opt.clone()),
                )),
            );
        }

        col = col.push(soundcore_card(
            widget::column![
                widget::text::body(fl!("db-refresh-rate")).size(15),
                pills,
            ]
            .spacing(12),
        ));
    }

    let presets: Vec<i32> = [75, 80, 85, 90, 95, 100]
        .into_iter()
        .filter(|v| limit_range.contains(v))
        .collect();
    let mut limit_pills = widget::row::with_capacity(presets.len()).spacing(8);
    for val in presets {
        let selected = val == current_limit;
        limit_pills = limit_pills.push(
            widget::button::custom(
                widget::text::body(format!("{val}dB"))
                    .size(12)
                    .align_x(iced::alignment::Horizontal::Center)
                    .width(Length::Fill),
            )
            .class(pill_button_style(selected && limit_enabled, false))
            .padding([8, 4])
            .width(Length::Fill)
            .on_press(DeviceMessage::SetSetting(
                SettingId::LimitHighVolumeDbLimit,
                Value::I32(val),
            )),
        );
    }

    col = col.push(soundcore_card(
        widget::column![
            widget::row![
                widget::column![
                    widget::text::body(fl!("limit-high-volume")).size(15),
                    secondary_text(fl!("limit-high-volume-description"), 12.0),
                ]
                .spacing(4)
                .width(Length::Fill),
                widget::toggler(limit_enabled).on_toggle(|enabled| {
                    DeviceMessage::SetSetting(
                        SettingId::LimitHighVolume,
                        Value::Bool(enabled),
                    )
                }),
            ]
            .spacing(12)
            .align_y(Alignment::Center),
            widget::row![
                crate::theme::accent_text(format!("{current_limit}dB"), 16.0)
                    .width(Length::Fill),
            ],
            limit_pills,
        ]
        .spacing(12),
    ));

    centered_page(col.padding([8, 0]))
}

trait PaletteStaticExt {
    fn from_color_cyan() -> Color;
}

impl PaletteStaticExt for SoundcorePalette {
    fn from_color_cyan() -> Color {
        crate::theme::SOUNDCORE_CYAN
    }
}

struct SmileyFaceCanvas {
    color: Color,
    level: VolumeStatusLevel,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for SmileyFaceCanvas {
    type State = ();

    fn draw(
        &self,
        _state: &Self::State,
        renderer: &cosmic::Renderer,
        _theme: &Theme,
        bounds: Rectangle,
        _cursor: iced::core::mouse::Cursor,
    ) -> Vec<widget::canvas::Geometry<cosmic::Renderer>> {
        let mut frame = widget::canvas::Frame::new(renderer, bounds.size());
        let w = bounds.width;
        let h = bounds.height;
        let cx = w / 2.0;
        let cy = h / 2.0;
        let r = w.min(h) / 2.0 - 1.0;

        frame.fill(&widget::canvas::Path::circle(Point::new(cx, cy), r), self.color);

        let eye_color = Color::from_rgb(0.08, 0.09, 0.11);
        frame.fill(
            &widget::canvas::Path::circle(Point::new(cx - r * 0.34, cy - r * 0.18), r * 0.12),
            eye_color,
        );
        frame.fill(
            &widget::canvas::Path::circle(Point::new(cx + r * 0.34, cy - r * 0.18), r * 0.12),
            eye_color,
        );

        let mouth = widget::canvas::Path::new(|b| {
            let ml = Point::new(cx - r * 0.38, cy + r * 0.2);
            let mr = Point::new(cx + r * 0.38, cy + r * 0.2);
            match self.level {
                VolumeStatusLevel::Normal => {
                    b.move_to(ml);
                    b.quadratic_curve_to(Point::new(cx, cy + r * 0.62), mr);
                }
                VolumeStatusLevel::Acceptable => {
                    b.move_to(Point::new(ml.x, cy + r * 0.3));
                    b.line_to(Point::new(mr.x, cy + r * 0.3));
                }
                VolumeStatusLevel::High => {
                    b.move_to(Point::new(ml.x, cy + r * 0.45));
                    b.quadratic_curve_to(
                        Point::new(cx, cy + r * 0.05),
                        Point::new(mr.x, cy + r * 0.45),
                    );
                }
            }
        });
        let mut stroke = widget::canvas::Stroke::default()
            .with_color(eye_color)
            .with_width(1.8);
        stroke.line_cap = widget::canvas::LineCap::Round;
        frame.stroke(&mouth, stroke);

        vec![frame.into_geometry()]
    }
}

struct DecibelMeterCanvas {
    current_db: f32,
    display_db_int: i32,
    limit_db: i32,
    wave_phase: f32,
    status_level: VolumeStatusLevel,
    has_reading: bool,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for DecibelMeterCanvas {
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

        let cx = bounds.width * 0.5;
        let top_y = 14.0;
        let tube_h = (bounds.height - 28.0).max(40.0);
        let tube_w = 56.0;
        let tube_left = cx - tube_w * 0.5;
        let tube_right = cx + tube_w * 0.5;
        let bottom_y = top_y + tube_h;

        let max_scale = 100.0_f32;
        let db_frac = (self.current_db / max_scale).clamp(0.0, 1.0);
        let limit_frac = (self.limit_db as f32 / max_scale).clamp(0.0, 1.0);

        // Scale ticks to the left of the tube
        let ticks_right = tube_left - 18.0;
        let ticks_left = ticks_right - 44.0;
        let active_color = match self.status_level {
            VolumeStatusLevel::High => SOUNDCORE_CORAL,
            VolumeStatusLevel::Acceptable => p.accent,
            VolumeStatusLevel::Normal => p.accent,
        };

        for db_step in (0..=100).step_by(2) {
            let frac = db_step as f32 / max_scale;
            let y = bottom_y - tube_h * frac;
            let is_major = db_step == 0 || db_step == self.limit_db || db_step == 100;
            let line_start = if is_major { ticks_left } else { ticks_right - 14.0 };
            let tick_color = if frac <= db_frac && self.has_reading {
                active_color
            } else {
                Color { a: 0.25, ..p.text_secondary }
            };
            frame.stroke(
                &widget::canvas::Path::line(Point::new(line_start, y), Point::new(ticks_right, y)),
                widget::canvas::Stroke::default()
                    .with_color(tick_color)
                    .with_width(if is_major { 1.4 } else { 1.0 }),
            );
        }

        // Left scale labels: 100dB, limitdB, 0dB
        frame.fill_text(widget::canvas::Text {
            content: "100dB".to_string(),
            position: Point::new(ticks_left, top_y - 3.0),
            color: p.text_secondary,
            size: iced::Pixels(11.0),
            align_x: TextAlignment::Left,
            align_y: Vertical::Bottom,
            ..Default::default()
        });
        let limit_y = bottom_y - tube_h * limit_frac;
        frame.fill_text(widget::canvas::Text {
            content: format!("{}dB", self.limit_db),
            position: Point::new(ticks_left, limit_y - 3.0),
            color: p.text_secondary,
            size: iced::Pixels(11.0),
            align_x: TextAlignment::Left,
            align_y: Vertical::Bottom,
            ..Default::default()
        });
        frame.fill_text(widget::canvas::Text {
            content: "0dB".to_string(),
            position: Point::new(ticks_left, bottom_y - 3.0),
            color: p.text_secondary,
            size: iced::Pixels(11.0),
            align_x: TextAlignment::Left,
            align_y: Vertical::Bottom,
            ..Default::default()
        });

        // Outer pill container
        let radius = iced::border::Radius::from(10.0);
        let tube_rect = widget::canvas::Path::rounded_rectangle(
            Point::new(tube_left, top_y),
            iced::Size::new(tube_w, tube_h),
            radius,
        );
        frame.fill(&tube_rect, p.surface_pill);

        // Liquid fill with two overlapping sine waves inside tube bounds
        let fill_surface_y = bottom_y - tube_h * db_frac;
        if db_frac > 0.015 {
            let (top_col, bot_col, back_col) = match self.status_level {
                VolumeStatusLevel::High => (
                    SOUNDCORE_CORAL,
                    SOUNDCORE_AMBER,
                    Color { a: 0.45, ..SOUNDCORE_CORAL },
                ),
                VolumeStatusLevel::Acceptable | VolumeStatusLevel::Normal => (
                    p.accent,
                    SOUNDCORE_BLUE,
                    Color { a: 0.42, ..SOUNDCORE_BLUE },
                ),
            };

            let amp = (tube_h * 0.018).clamp(2.5, 4.5);
            let inset = 1.5;
            let l = tube_left + inset;
            let r = tube_right - inset;
            let b_y = bottom_y - inset;
            let samples = 24;

            let back_wave = widget::canvas::Path::new(|b| {
                b.move_to(Point::new(l, b_y));
                for i in 0..=samples {
                    let t = i as f32 / samples as f32;
                    let x = l + (r - l) * t;
                    let wy = (fill_surface_y
                        + amp * (t * std::f32::consts::TAU * 1.1 - self.wave_phase * 0.85 + 1.4).sin())
                    .clamp(top_y + 4.0, b_y);
                    b.line_to(Point::new(x, wy));
                }
                b.line_to(Point::new(r, b_y));
                b.close();
            });
            frame.fill(&back_wave, back_col);

            let front_wave = widget::canvas::Path::new(|b| {
                b.move_to(Point::new(l, b_y));
                for i in 0..=samples {
                    let t = i as f32 / samples as f32;
                    let x = l + (r - l) * t;
                    let wy = (fill_surface_y
                        + amp * (t * std::f32::consts::TAU * 1.2 + self.wave_phase).sin())
                    .clamp(top_y + 4.0, b_y);
                    b.line_to(Point::new(x, wy));
                }
                b.line_to(Point::new(r, b_y));
                b.close();
            });
            let liq_grad = widget::canvas::gradient::Linear::new(
                Point::new(0.0, fill_surface_y),
                Point::new(0.0, b_y),
            )
            .add_stop(0.0, top_col)
            .add_stop(1.0, bot_col);
            frame.fill(
                &front_wave,
                widget::canvas::gradient::Gradient::Linear(liq_grad),
            );
        }

        // Crisp tube outline over top
        frame.stroke(
            &tube_rect,
            widget::canvas::Stroke::default()
                .with_color(p.divider)
                .with_width(1.2),
        );

        // Right current dB pointer + value
        let ptr_y = fill_surface_y.clamp(top_y + 12.0, bottom_y - 12.0);
        let arrow_x = tube_right + 12.0;
        let tri = widget::canvas::Path::new(|b| {
            b.move_to(Point::new(arrow_x, ptr_y - 4.5));
            b.line_to(Point::new(arrow_x + 7.0, ptr_y));
            b.line_to(Point::new(arrow_x, ptr_y + 4.5));
            b.close();
        });
        frame.fill(&tri, p.text_primary);

        let value_str = if self.has_reading {
            format!("{} dB", self.display_db_int)
        } else {
            "-- dB".to_string()
        };
        frame.fill_text(widget::canvas::Text {
            content: value_str,
            position: Point::new(arrow_x + 14.0, ptr_y),
            color: p.text_primary,
            size: iced::Pixels(22.0),
            font: cosmic::font::bold(),
            align_x: TextAlignment::Left,
            align_y: Vertical::Center,
            ..Default::default()
        });

        vec![frame.into_geometry()]
    }
}
