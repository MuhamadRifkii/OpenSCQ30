use cosmic::{
    Element, Theme,
    iced::{self, Color, Point, Rectangle, Size},
    widget,
};

use crate::theme::SoundcorePalette;

pub fn earbuds_artwork<'a, M: 'a>(size: f32) -> Element<'a, M> {
    widget::canvas(EarbudsArtworkCanvas { compact: false })
        .width(size)
        .height(size * 0.78)
        .into()
}

pub fn compact_earbuds_artwork<'a, M: 'a>(size: f32) -> Element<'a, M> {
    widget::canvas(EarbudsArtworkCanvas { compact: true })
        .width(size)
        .height(size)
        .into()
}

struct EarbudsArtworkCanvas {
    compact: bool,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for EarbudsArtworkCanvas {
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

        let body_color = if p.is_dark {
            Color::from_rgb(0.19, 0.21, 0.25)
        } else {
            Color::from_rgb(0.24, 0.26, 0.31)
        };
        let highlight = if p.is_dark {
            Color::from_rgb(0.30, 0.33, 0.38)
        } else {
            Color::from_rgb(0.38, 0.41, 0.46)
        };
        let tip_color = if p.is_dark {
            Color::from_rgb(0.12, 0.13, 0.16)
        } else {
            Color::from_rgb(0.16, 0.18, 0.22)
        };
        let stroke_color = if p.is_dark {
            Color::from_rgba(1.0, 1.0, 1.0, 0.14)
        } else {
            Color::from_rgba(0.0, 0.0, 0.0, 0.18)
        };

        let unit = w.min(h / 0.85);
        let cx = w * 0.5;
        let cy = h * 0.46;

        // Left earbud shifted left-up
        draw_earbud(
            &mut frame,
            Point::new(cx - unit * 0.19, cy - unit * 0.04),
            unit,
            -1.0,
            body_color,
            highlight,
            tip_color,
            stroke_color,
            p.accent,
            self.compact,
        );

        // Right earbud shifted right-down
        draw_earbud(
            &mut frame,
            Point::new(cx + unit * 0.19, cy + unit * 0.06),
            unit,
            1.0,
            body_color,
            highlight,
            tip_color,
            stroke_color,
            p.accent,
            self.compact,
        );

        vec![frame.into_geometry()]
    }
}

#[allow(clippy::too_many_arguments)]
fn draw_earbud(
    frame: &mut widget::canvas::Frame<cosmic::Renderer>,
    head_center: Point,
    unit: f32,
    dir: f32,
    body_color: Color,
    highlight: Color,
    tip_color: Color,
    stroke_color: Color,
    accent: Color,
    compact: bool,
) {
    let head_r = unit * 0.155;
    let stem_w = unit * 0.085;
    let stem_h = unit * 0.33;

    // Ear tip facing outward/inward naturally (dir < 0 = left earbud pointing left)
    let tip_center = Point::new(head_center.x + dir * (head_r * 0.86), head_center.y + head_r * 0.12);
    let tip_path = widget::canvas::Path::circle(tip_center, head_r * 0.55);
    frame.fill(&tip_path, tip_color);

    // Stem angling downward toward center
    let stem_top = Point::new(head_center.x - stem_w * 0.5, head_center.y + head_r * 0.35);
    let stem_tilt = -dir * unit * 0.03;
    let stem = widget::canvas::Path::new(|b| {
        b.move_to(Point::new(stem_top.x, stem_top.y));
        b.line_to(Point::new(stem_top.x + stem_w, stem_top.y));
        b.line_to(Point::new(stem_top.x + stem_w + stem_tilt, stem_top.y + stem_h));
        b.bezier_curve_to(
            Point::new(stem_top.x + stem_w * 0.8 + stem_tilt, stem_top.y + stem_h + stem_w * 0.55),
            Point::new(stem_top.x + stem_w * 0.2 + stem_tilt, stem_top.y + stem_h + stem_w * 0.55),
            Point::new(stem_top.x + stem_tilt, stem_top.y + stem_h),
        );
        b.close();
    });
    frame.fill(&stem, body_color);
    frame.stroke(
        &stem,
        widget::canvas::Stroke::default()
            .with_color(stroke_color)
            .with_width(1.0),
    );

    // Head sphere
    let head = widget::canvas::Path::circle(head_center, head_r);
    frame.fill(&head, body_color);
    frame.stroke(
        &head,
        widget::canvas::Stroke::default()
            .with_color(stroke_color)
            .with_width(1.1),
    );

    // Subtle sheen highlight on upper outer contour
    let sheen = widget::canvas::Path::circle(
        Point::new(head_center.x - dir * head_r * 0.25, head_center.y - head_r * 0.25),
        head_r * 0.42,
    );
    frame.fill(&sheen, Color { a: 0.22, ..highlight });

    // Stem accent strip / touch ring
    if !compact {
        let line = widget::canvas::Path::line(
            Point::new(head_center.x + stem_tilt * 0.4, stem_top.y + stem_h * 0.22),
            Point::new(head_center.x + stem_tilt * 0.75, stem_top.y + stem_h * 0.65),
        );
        frame.stroke(
            &line,
            widget::canvas::Stroke::default()
                .with_color(Color { a: 0.55, ..accent })
                .with_width(1.8),
        );
    }

    // Touch sensor ring on head
    let ring = widget::canvas::Path::circle(head_center, head_r * 0.28);
    frame.stroke(
        &ring,
        widget::canvas::Stroke::default()
            .with_color(Color { a: 0.55, ..highlight })
            .with_width(1.2),
    );

    let _ = Size::ZERO;
}
