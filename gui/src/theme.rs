use std::{borrow::Cow, sync::Arc};

use cosmic::{
    Element, Theme,
    cosmic_theme::{
        ThemeBuilder,
        palette::{Srgb, Srgba},
    },
    iced::{
        self, Alignment, Background, Color, Length, Radians, Shadow,
        core::Border,
        gradient::Linear,
    },
    theme, widget,
};

use crate::{config::AppTheme, icons};

pub const SOUNDCORE_CYAN: Color = Color::from_rgb(0.078, 0.784, 0.910);
pub const SOUNDCORE_CYAN_BRIGHT: Color = Color::from_rgb(0.180, 0.878, 0.976);
pub const SOUNDCORE_PINK: Color = Color::from_rgb(0.925, 0.365, 0.745);
pub const SOUNDCORE_GREEN: Color = Color::from_rgb(0.247, 0.839, 0.404);
pub const SOUNDCORE_BLUE: Color = Color::from_rgb(0.102, 0.471, 0.949);
pub const SOUNDCORE_AMBER: Color = Color::from_rgb(0.906, 0.714, 0.298);
pub const SOUNDCORE_CORAL: Color = Color::from_rgb(0.941, 0.482, 0.322);

#[derive(Clone, Copy, Debug)]
pub struct SoundcorePalette {
    pub is_dark: bool,
    pub background: Color,
    pub surface_card: Color,
    pub surface_card_hover: Color,
    pub surface_elevated: Color,
    pub surface_pill: Color,
    pub divider: Color,
    pub accent: Color,
    pub on_accent: Color,
    pub green: Color,
    pub gradient_pink: Color,
    pub gradient_cyan: Color,
    pub text_primary: Color,
    pub text_secondary: Color,
    pub text_tertiary: Color,
}

impl SoundcorePalette {
    pub fn from_theme(theme: &Theme) -> Self {
        let cosmic = theme.cosmic();
        let is_dark = theme.theme_type.is_dark();
        let bg = Color::from(cosmic.bg_color());
        let is_amoled = is_dark && bg.r < 0.01 && bg.g < 0.01 && bg.b < 0.01;
        let accent: Color = cosmic.accent_color().into();
        let on_accent: Color = cosmic.on_accent_color().into();

        if is_dark {
            let card = if is_amoled {
                Color::from_rgb(0.075, 0.082, 0.102)
            } else {
                Color::from_rgb(0.102, 0.114, 0.141)
            };
            let card_hover = Color::from_rgb(card.r + 0.035, card.g + 0.035, card.b + 0.04);
            let elevated = Color::from_rgb(0.141, 0.157, 0.192);
            let pill = Color::from_rgb(0.165, 0.180, 0.220);
            Self {
                is_dark: true,
                background: bg,
                surface_card: card,
                surface_card_hover: card_hover,
                surface_elevated: elevated,
                surface_pill: pill,
                divider: Color::from_rgba(1.0, 1.0, 1.0, 0.09),
                accent,
                on_accent,
                green: SOUNDCORE_GREEN,
                gradient_pink: SOUNDCORE_PINK,
                gradient_cyan: SOUNDCORE_CYAN_BRIGHT,
                text_primary: Color::from_rgb(0.953, 0.961, 0.976),
                text_secondary: Color::from_rgb(0.620, 0.651, 0.706),
                text_tertiary: Color::from_rgb(0.412, 0.443, 0.498),
            }
        } else {
            Self {
                is_dark: false,
                background: bg,
                surface_card: Color::WHITE,
                surface_card_hover: Color::from_rgb(0.945, 0.957, 0.973),
                surface_elevated: Color::from_rgb(0.925, 0.937, 0.957),
                surface_pill: Color::from_rgb(0.902, 0.918, 0.941),
                divider: Color::from_rgba(0.0, 0.0, 0.0, 0.09),
                accent,
                on_accent,
                green: Color::from_rgb(0.133, 0.651, 0.286),
                gradient_pink: Color::from_rgb(0.847, 0.255, 0.659),
                gradient_cyan: accent,
                text_primary: Color::from_rgb(0.075, 0.090, 0.118),
                text_secondary: Color::from_rgb(0.365, 0.400, 0.463),
                text_tertiary: Color::from_rgb(0.569, 0.600, 0.655),
            }
        }
    }
}

pub fn resolve_cosmic_theme(app_theme: AppTheme, system_theme: Option<&Theme>) -> Theme {
    match app_theme {
        AppTheme::System => system_theme
            .cloned()
            .unwrap_or_else(cosmic::theme::system_preference),
        AppTheme::Light => {
            let built = ThemeBuilder::light()
                .bg_color(Srgba::new(0.961, 0.969, 0.980, 1.0))
                .primary_container_bg(Srgba::new(1.0, 1.0, 1.0, 1.0))
                .accent(Srgb::new(0.0, 0.612, 0.722))
                .build();
            Theme::custom(Arc::new(built))
        }
        AppTheme::Dark => Theme::dark(),
        AppTheme::SoundcoreDark => {
            let built = ThemeBuilder::dark()
                .bg_color(Srgba::new(0.047, 0.055, 0.071, 1.0))
                .primary_container_bg(Srgba::new(0.102, 0.114, 0.141, 1.0))
                .accent(Srgb::new(0.078, 0.784, 0.910))
                .build();
            Theme::custom(Arc::new(built))
        }
        AppTheme::AmoledBlack => {
            let built = ThemeBuilder::dark()
                .bg_color(Srgba::new(0.0, 0.0, 0.0, 1.0))
                .primary_container_bg(Srgba::new(0.075, 0.082, 0.102, 1.0))
                .accent(Srgb::new(0.078, 0.784, 0.910))
                .build();
            Theme::custom(Arc::new(built))
        }
    }
}

pub fn card_container_style(theme: &Theme) -> widget::container::Style {
    let p = SoundcorePalette::from_theme(theme);
    widget::container::Style {
        icon_color: Some(p.text_primary),
        text_color: Some(p.text_primary),
        background: Some(Background::Color(p.surface_card)),
        border: Border {
            color: p.divider,
            width: if p.is_dark { 0.5 } else { 1.0 },
            radius: 18.0.into(),
        },
        shadow: Shadow {
            color: Color::TRANSPARENT,
            offset: iced::Vector::new(0.0, 0.0),
            blur_radius: 0.0,
        },
        snap: true,
    }
}

pub fn soundcore_card<'a, M: 'a>(
    content: impl Into<Element<'a, M>>,
) -> widget::Container<'a, M, Theme, cosmic::Renderer> {
    widget::container(content)
        .width(Length::Fill)
        .padding(16)
        .class(theme::Container::custom(card_container_style))
}

pub fn soundcore_grouped_card<'a, M: 'a>(
    content: impl Into<Element<'a, M>>,
) -> widget::Container<'a, M, Theme, cosmic::Renderer> {
    widget::container(content)
        .width(Length::Fill)
        .class(theme::Container::custom(card_container_style))
}

pub fn card_button_style(radius: f32) -> widget::button::ButtonClass {
    widget::button::ButtonClass::Custom {
        active: Box::new(move |focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_card)),
                overlay: None,
                border_radius: radius.into(),
                border_width: if focused { 1.5 } else if p.is_dark { 0.5 } else { 1.0 },
                border_color: if focused { p.accent } else { p.divider },
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
        disabled: Box::new(move |theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_card)),
                overlay: None,
                border_radius: radius.into(),
                border_width: 0.5,
                border_color: p.divider,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_tertiary),
                text_color: Some(p.text_tertiary),
            }
        }),
        hovered: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_card_hover)),
                overlay: None,
                border_radius: radius.into(),
                border_width: if p.is_dark { 0.75 } else { 1.0 },
                border_color: p.divider,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
        pressed: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_elevated)),
                overlay: None,
                border_radius: radius.into(),
                border_width: 1.0,
                border_color: p.accent,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
    }
}

pub fn transparent_row_button_style(radius: f32) -> widget::button::ButtonClass {
    widget::button::ButtonClass::Custom {
        active: Box::new(move |focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: None,
                overlay: None,
                border_radius: radius.into(),
                border_width: if focused { 1.0 } else { 0.0 },
                border_color: if focused { p.accent } else { Color::TRANSPARENT },
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
        disabled: Box::new(move |theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: None,
                overlay: None,
                border_radius: radius.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_tertiary),
                text_color: Some(p.text_tertiary),
            }
        }),
        hovered: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_elevated)),
                overlay: None,
                border_radius: radius.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
        pressed: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_pill)),
                overlay: None,
                border_radius: radius.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_primary),
                text_color: Some(p.text_primary),
            }
        }),
    }
}

pub fn pill_button_style(selected: bool, gradient_active: bool) -> widget::button::ButtonClass {
    widget::button::ButtonClass::Custom {
        active: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            let bg = if selected && gradient_active {
                let lin = Linear::new(Radians(std::f32::consts::FRAC_PI_2))
                    .add_stop(0.0, p.gradient_pink)
                    .add_stop(1.0, p.gradient_cyan);
                Background::Gradient(lin.into())
            } else if selected {
                Background::Color(p.accent)
            } else {
                Background::Color(p.surface_pill)
            };
            let fg = if selected { p.on_accent } else { p.text_primary };
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(bg),
                overlay: None,
                border_radius: 22.0.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(fg),
                text_color: Some(fg),
            }
        }),
        disabled: Box::new(move |theme| {
            let p = SoundcorePalette::from_theme(theme);
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(p.surface_pill)),
                overlay: None,
                border_radius: 22.0.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(p.text_tertiary),
                text_color: Some(p.text_tertiary),
            }
        }),
        hovered: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            let bg = if selected && gradient_active {
                let lin = Linear::new(Radians(std::f32::consts::FRAC_PI_2))
                    .add_stop(0.0, p.gradient_pink)
                    .add_stop(1.0, p.gradient_cyan);
                Background::Gradient(lin.into())
            } else if selected {
                Background::Color(p.accent)
            } else {
                Background::Color(p.surface_card_hover)
            };
            let fg = if selected { p.on_accent } else { p.text_primary };
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(bg),
                overlay: None,
                border_radius: 22.0.into(),
                border_width: 1.0,
                border_color: p.accent,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(fg),
                text_color: Some(fg),
            }
        }),
        pressed: Box::new(move |_focused, theme| {
            let p = SoundcorePalette::from_theme(theme);
            let bg = if selected { p.accent } else { p.surface_elevated };
            let fg = if selected { p.on_accent } else { p.text_primary };
            widget::button::Style {
                shadow_offset: iced::Vector::new(0.0, 0.0),
                background: Some(Background::Color(bg)),
                overlay: None,
                border_radius: 22.0.into(),
                border_width: 0.0,
                border_color: Color::TRANSPARENT,
                outline_width: 0.0,
                outline_color: Color::TRANSPARENT,
                icon_color: Some(fg),
                text_color: Some(fg),
            }
        }),
    }
}

pub fn accent_cta_button_style() -> widget::button::ButtonClass {
    pill_button_style(true, false)
}

pub fn soundcore_divider<'a, M: 'a>() -> Element<'a, M> {
    widget::container(widget::divider::horizontal::default())
        .padding([0, 16])
        .width(Length::Fill)
        .into()
}

pub fn radio_indicator<'a, M: 'a>(selected: bool) -> Element<'a, M> {
    widget::canvas(RadioIndicatorCanvas { selected })
        .width(22)
        .height(22)
        .into()
}

struct RadioIndicatorCanvas {
    selected: bool,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for RadioIndicatorCanvas {
    type State = ();

    fn draw(
        &self,
        _state: &Self::State,
        renderer: &cosmic::Renderer,
        theme: &Theme,
        bounds: iced::Rectangle,
        _cursor: iced::core::mouse::Cursor,
    ) -> Vec<widget::canvas::Geometry<cosmic::Renderer>> {
        let p = SoundcorePalette::from_theme(theme);
        let mut frame = widget::canvas::Frame::new(renderer, bounds.size());
        let center = iced::Point::new(bounds.width / 2.0, bounds.height / 2.0);
        let radius = (bounds.width.min(bounds.height) / 2.0) - 1.5;

        if self.selected {
            frame.fill(
                &widget::canvas::Path::circle(center, radius),
                p.accent,
            );
            frame.fill(
                &widget::canvas::Path::circle(center, radius * 0.45),
                p.on_accent,
            );
        } else {
            frame.stroke(
                &widget::canvas::Path::circle(center, radius),
                widget::canvas::Stroke::default()
                    .with_color(p.text_tertiary)
                    .with_width(1.6),
            );
        }
        vec![frame.into_geometry()]
    }
}

pub fn nav_row<'a, M: Clone + 'a>(
    title: impl Into<Cow<'a, str>> + 'a,
    subtitle: Option<Cow<'a, str>>,
    value_text: Option<Cow<'a, str>>,
    leading_icon: Option<Element<'a, M>>,
    on_press: M,
) -> Element<'a, M> {
    let mut text_col = widget::column::with_capacity(2)
        .push(widget::text::body(title).size(15))
        .spacing(2);
    if let Some(sub) = subtitle {
        text_col = text_col.push(
            widget::text::caption(sub)
                .size(12)
                .class(theme::Text::Custom(|theme| {
                    let p = SoundcorePalette::from_theme(theme);
                    iced::widget::text::Style {
                        color: Some(p.text_secondary),
                        ..Default::default()
                    }
                })),
        );
    }

    let row = widget::row::with_capacity(4)
        .push_maybe(leading_icon)
        .push(text_col.width(Length::Fill))
        .push_maybe(value_text.map(|val| {
            widget::text::body(val)
                .size(13)
                .class(theme::Text::Custom(|theme| {
                    let p = SoundcorePalette::from_theme(theme);
                    iced::widget::text::Style {
                        color: Some(p.text_secondary),
                        ..Default::default()
                    }
                }))
        }))
        .push(icons::chevron_right_tinted(18))
        .spacing(12)
        .align_y(Alignment::Center)
        .width(Length::Fill);

    widget::button::custom(row)
        .class(transparent_row_button_style(16.0))
        .padding([14, 16])
        .width(Length::Fill)
        .on_press(on_press)
        .into()
}

pub fn secondary_text<'a>(
    content: impl Into<Cow<'a, str>> + 'a,
    size: f32,
) -> widget::Text<'a, Theme, cosmic::Renderer> {
    widget::text::text(content)
        .size(size)
        .class(theme::Text::Custom(|theme| {
            let p = SoundcorePalette::from_theme(theme);
            iced::widget::text::Style {
                color: Some(p.text_secondary),
                ..Default::default()
            }
        }))
}

pub fn accent_text<'a>(
    content: impl Into<Cow<'a, str>> + 'a,
    size: f32,
) -> widget::Text<'a, Theme, cosmic::Renderer> {
    widget::text::text(content)
        .size(size)
        .font(cosmic::font::semibold())
        .class(theme::Text::Custom(|theme| {
            let p = SoundcorePalette::from_theme(theme);
            iced::widget::text::Style {
                color: Some(p.accent),
                ..Default::default()
            }
        }))
}

pub fn centered_page<'a, M: 'a>(
    content: impl Into<Element<'a, M>>,
) -> Element<'a, M> {
    widget::scrollable(
        widget::container(
            widget::container(content)
                .max_width(640)
                .width(Length::Fill),
        )
        .width(Length::Fill)
        .align_x(iced::alignment::Horizontal::Center)
        .padding([12, 20]),
    )
    .width(Length::Fill)
    .height(Length::Fill)
    .into()
}
