use cosmic::{
    Element, Theme,
    iced::{
        self, Alignment, Background, Border, Color, Length, Point, Rectangle,
        alignment::Vertical,
        core::text::Alignment as TextAlignment,
    },
    theme, widget,
};

use crate::{
    icons,
    soundcore::helpers::BatterySummary,
    theme::{SOUNDCORE_CORAL, SoundcorePalette},
};

pub fn ear_badge<'a, M: 'a>(label: &'static str, size: u16) -> Element<'a, M> {
    widget::canvas(EarBadgeCanvas { label })
        .width(size)
        .height(size)
        .into()
}

pub fn case_badge<'a, M: 'a>(size: u16) -> Element<'a, M> {
    widget::canvas(CaseBadgeCanvas)
        .width(size)
        .height(size)
        .into()
}

struct EarBadgeCanvas {
    label: &'static str,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for EarBadgeCanvas {
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
        let center = Point::new(bounds.width / 2.0, bounds.height / 2.0);
        let radius = (bounds.width.min(bounds.height) / 2.0) - 1.0;

        frame.stroke(
            &widget::canvas::Path::circle(center, radius),
            widget::canvas::Stroke::default()
                .with_color(p.text_secondary)
                .with_width(1.3),
        );
        frame.fill_text(widget::canvas::Text {
            content: self.label.to_string(),
            position: Point::new(center.x, center.y + 0.5),
            color: p.text_primary,
            size: iced::Pixels((bounds.height * 0.52).max(9.0)),
            font: cosmic::font::semibold(),
            align_x: TextAlignment::Center,
            align_y: Vertical::Center,
            ..Default::default()
        });

        vec![frame.into_geometry()]
    }
}

struct CaseBadgeCanvas;

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for CaseBadgeCanvas {
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
        let rect = widget::canvas::Path::rectangle(
            Point::new(w * 0.16, h * 0.22),
            iced::Size::new(w * 0.68, h * 0.56),
        );
        frame.stroke(
            &rect,
            widget::canvas::Stroke::default()
                .with_color(p.text_secondary)
                .with_width(1.3),
        );
        let lid = widget::canvas::Path::line(
            Point::new(w * 0.16, h * 0.44),
            Point::new(w * 0.84, h * 0.44),
        );
        frame.stroke(
            &lid,
            widget::canvas::Stroke::default()
                .with_color(p.text_secondary)
                .with_width(1.1),
        );
        vec![frame.into_geometry()]
    }
}

pub fn battery_strip<'a, M: 'a>(summary: &BatterySummary) -> Element<'a, M> {
    battery_strip_inner(summary, false)
}

pub fn compact_battery_strip<'a, M: 'a>(summary: &BatterySummary) -> Element<'a, M> {
    battery_strip_inner(summary, true)
}

fn battery_strip_inner<'a, M: 'a>(summary: &BatterySummary, compact: bool) -> Element<'a, M> {
    let mut items = Vec::new();
    let badge_size = if compact { 14 } else { 16 };
    let bar_width = if compact { 44.0 } else { 58.0 };

    if let Some(left) = summary.left {
        items.push(battery_item(
            ear_badge("L", badge_size),
            left,
            summary.left_charging,
            bar_width,
            compact,
        ));
    }
    if let Some(right) = summary.right {
        items.push(battery_item(
            ear_badge("R", badge_size),
            right,
            summary.right_charging,
            bar_width,
            compact,
        ));
    }
    if let Some(case_level) = summary.case_level {
        items.push(battery_item(
            case_badge(badge_size),
            case_level,
            false,
            bar_width,
            compact,
        ));
    }
    if items.is_empty()
        && let Some(single) = summary.single
    {
        items.push(battery_item(
            icons::tinted_secondary(icons::noise_control(), badge_size),
            single,
            summary.left_charging,
            bar_width,
            compact,
        ));
    }

    widget::row::with_children(items)
        .spacing(if compact { 12 } else { 20 })
        .align_y(Alignment::Center)
        .into()
}

fn battery_item<'a, M: 'a>(
    badge: Element<'a, M>,
    percent: u8,
    charging: bool,
    bar_width: f32,
    compact: bool,
) -> Element<'a, M> {
    let pct_text = format!("{percent}%");
    let fraction = (f32::from(percent) / 100.0).clamp(0.04, 1.0);
    let fill_width = bar_width * fraction;

    let label_row = widget::row::with_capacity(3)
        .push(badge)
        .push(widget::text::body(pct_text).size(if compact { 12 } else { 13 }))
        .push_maybe(charging.then(|| icons::tinted_accent(icons::bolt(), 13)))
        .spacing(4)
        .align_y(Alignment::Center);

    let bar = widget::container(
        widget::container(widget::space().width(Length::Fill))
            .width(Length::Fixed(fill_width))
            .height(Length::Fixed(3.0))
            .class(theme::Container::custom(move |theme| {
                let p = SoundcorePalette::from_theme(theme);
                let color = if percent <= 20 {
                    SOUNDCORE_CORAL
                } else {
                    p.green
                };
                widget::container::Style {
                    background: Some(Background::Color(color)),
                    border: Border {
                        radius: 2.0.into(),
                        ..Default::default()
                    },
                    ..Default::default()
                }
            })),
    )
    .width(Length::Fixed(bar_width))
    .height(Length::Fixed(3.0))
    .class(theme::Container::custom(|theme| {
        let p = SoundcorePalette::from_theme(theme);
        widget::container::Style {
            background: Some(Background::Color(p.surface_pill)),
            border: Border {
                radius: 2.0.into(),
                ..Default::default()
            },
            ..Default::default()
        }
    }));

    widget::column![label_row, bar]
        .spacing(4)
        .align_x(Alignment::Center)
        .into()
}
