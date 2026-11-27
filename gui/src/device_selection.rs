use std::sync::Arc;

use cosmic::{
    Element, Task, Theme,
    iced::{self, Alignment, Background, Color, Length, Point, Rectangle},
    theme, widget,
};
use macaddr::MacAddr6;
use openscq30_i18n::Translate;
use openscq30_lib::{OpenSCQ30Session, storage::PairedDevice};

use crate::{
    fl, handle_soft_error, icons,
    soundcore::{artwork, battery, helpers::BatterySummary},
    theme::{
        SOUNDCORE_GREEN, SoundcorePalette, card_button_style, centered_page, pill_button_style,
        secondary_text, soundcore_card, transparent_row_button_style,
    },
    utils::coalesce_result,
};

pub struct DeviceSelectionModel {
    paired_devices: Vec<PairedDevice>,
}

#[derive(Debug, Clone)]
pub enum Message {
    ConnectToDevice(usize),
    CancelConnect,
    RemoveDevice(usize),
    AddDevice,
    SetPairedDevices(Vec<PairedDevice>),
    Warning(String),
}

pub enum Action {
    ConnectToDevice(PairedDevice),
    CancelConnect,
    RemoveDevice(PairedDevice),
    AddDevice,
    None,
    Warning(String),
}

impl DeviceSelectionModel {
    pub fn new(session: Arc<OpenSCQ30Session>) -> (Self, Task<Message>) {
        let model = Self {
            paired_devices: Vec::new(),
        };
        (model, Self::refresh_paired_devices(session))
    }

    pub fn refresh_paired_devices(session: Arc<OpenSCQ30Session>) -> Task<Message> {
        Task::future(async move {
            Ok(Message::SetPairedDevices(
                session
                    .paired_devices()
                    .await
                    .map_err(handle_soft_error!())?,
            ))
        })
        .map(coalesce_result)
    }

    pub fn view_with_status<'a>(
        &'a self,
        active_mac: Option<MacAddr6>,
        active_battery: Option<BatterySummary>,
        connecting_mac: Option<MacAddr6>,
    ) -> Element<'a, Message> {
        let mut col = widget::column::with_capacity(self.paired_devices.len() + 3).spacing(14);
        col = col.push(
            widget::container(
                widget::text::title2(fl!("my-devices")).font(cosmic::font::bold()),
            )
            .padding([4, 2]),
        );

        if self.paired_devices.is_empty() {
            col = col.push(soundcore_card(
                widget::column![
                    artwork::earbuds_artwork(120.0),
                    widget::text::title4(fl!("no-paired-devices"))
                        .font(cosmic::font::semibold()),
                    secondary_text(fl!("add-device-prompt"), 13),
                ]
                .spacing(8)
                .align_x(Alignment::Center)
                .width(Length::Fill),
            ));
        } else {
            for (index, device) in self.paired_devices.iter().enumerate() {
                let is_connected = active_mac == Some(device.mac_address);
                let is_connecting = connecting_mac == Some(device.mac_address);
                let card_battery = is_connected.then_some(active_battery).flatten();
                col = col.push(device_card(
                    index,
                    device,
                    is_connected,
                    is_connecting,
                    card_battery,
                ));
            }
        }

        let add_card = widget::button::custom(
            widget::row![
                icons::tinted_primary(icons::add(), 18),
                widget::text::body(fl!("add-device"))
                    .size(15)
                    .font(cosmic::font::semibold()),
            ]
            .spacing(8)
            .align_y(Alignment::Center)
            .width(Length::Fill),
        )
        .class(card_button_style(18.0))
        .padding([16, 20])
        .width(Length::Fill)
        .on_press(Message::AddDevice);

        // Center "+ Add Device" cleanly inside card button
        let _ = add_card;
        let centered_add_card = widget::button::custom(
            widget::container(
                widget::row![
                    icons::tinted_primary(icons::add(), 18),
                    widget::text::body(fl!("add-device"))
                        .size(15)
                        .font(cosmic::font::semibold()),
                ]
                .spacing(8)
                .align_y(Alignment::Center),
            )
            .center_x(Length::Fill),
        )
        .class(card_button_style(18.0))
        .padding([16, 20])
        .width(Length::Fill)
        .on_press(Message::AddDevice);

        col = col.push(centered_add_card);

        centered_page(col.padding([8, 0]))
    }

    #[must_use]
    pub fn update(&mut self, message: Message) -> Action {
        match message {
            Message::ConnectToDevice(index) => {
                if let Some(device) = self.paired_devices.get(index).copied() {
                    return Action::ConnectToDevice(device);
                }
            }
            Message::CancelConnect => return Action::CancelConnect,
            Message::RemoveDevice(index) => {
                if let Some(device) = self.paired_devices.get(index).copied() {
                    return Action::RemoveDevice(device);
                }
            }
            Message::AddDevice => return Action::AddDevice,
            Message::SetPairedDevices(paired_devices) => self.paired_devices = paired_devices,
            Message::Warning(message) => return Action::Warning(message),
        }
        Action::None
    }
}

fn device_card<'a>(
    index: usize,
    device: &PairedDevice,
    is_connected: bool,
    is_connecting: bool,
    battery_summary: Option<BatterySummary>,
) -> Element<'a, Message> {
    let top_trailing: Option<Element<'a, Message>> = if is_connecting {
        Some(
            widget::container(widget::progress_bar::indeterminate_circular().size(18.0))
                .align_y(Alignment::Center)
                .into(),
        )
    } else if is_connected
        && let Some(ref summary) = battery_summary
        && summary.has_any()
    {
        Some(battery::compact_battery_strip(summary))
    } else {
        None
    };

    let status_row: Element<'a, Message> = if is_connecting {
        widget::row![
            widget::canvas(StatusDotCanvas { active: true })
                .width(8)
                .height(8),
            widget::text::caption(fl!("connecting"))
                .size(13)
                .width(Length::Fill),
            widget::button::custom(widget::text::caption(fl!("cancel")).size(11))
                .class(pill_button_style(false, false))
                .padding([4, 10])
                .on_press(Message::CancelConnect),
        ]
        .spacing(6)
        .align_y(Alignment::Center)
        .into()
    } else if is_connected {
        widget::row![
            widget::canvas(StatusDotCanvas { active: true })
                .width(8)
                .height(8),
            widget::text::body(fl!("connected")).size(13).class(theme::Text::Custom(
                |theme| {
                    let p = SoundcorePalette::from_theme(theme);
                    iced::widget::text::Style {
                        color: Some(p.text_secondary),
                    }
                },
            )),
            secondary_text(device.mac_address.to_string(), 11).width(Length::Fill),
        ]
        .spacing(6)
        .align_y(Alignment::Center)
        .into()
    } else {
        widget::row![
            secondary_text(fl!("not-connected"), 13),
            secondary_text(device.mac_address.to_string(), 11).width(Length::Fill),
        ]
        .spacing(8)
        .align_y(Alignment::Center)
        .into()
    };

    let mut title_line = widget::row::with_capacity(3)
        .push(
            widget::text::title4(device.model.translate())
                .font(cosmic::font::bold())
                .width(Length::Fill),
        )
        .spacing(8)
        .align_y(Alignment::Center);

    if device.is_demo {
        title_line = title_line.push(
            widget::container(
                widget::text::caption(fl!("demo-mode"))
                    .size(10)
                    .font(cosmic::font::semibold()),
            )
            .padding([2, 6])
            .class(theme::Container::custom(|theme| {
                let p = SoundcorePalette::from_theme(theme);
                widget::container::Style {
                    background: Some(Background::Color(p.surface_pill)),
                    border: cosmic::iced::Border {
                        radius: 6.0.into(),
                        ..Default::default()
                    },
                    text_color: Some(p.text_secondary),
                    ..Default::default()
                }
            })),
        );
    }

    if let Some(trailing) = top_trailing {
        title_line = title_line.push(trailing);
    }

    let info_col = widget::column![title_line, status_row]
        .spacing(6)
        .width(Length::Fill);

    let card_row = widget::row![
        artwork::compact_earbuds_artwork(60.0),
        info_col,
    ]
    .spacing(16)
    .align_y(Alignment::Center)
    .width(Length::Fill);

    let main_btn = widget::button::custom(card_row.width(Length::Fill))
        .class(card_button_style(18.0))
        .padding([16, 16])
        .width(Length::Fill)
        .on_press(Message::ConnectToDevice(index));

    let remove_btn = widget::button::custom(icons::tinted_secondary(icons::close(), 16))
        .class(transparent_row_button_style(12.0))
        .padding([8, 8])
        .on_press(Message::RemoveDevice(index));

    widget::row![main_btn, remove_btn]
        .spacing(6)
        .align_y(Alignment::Center)
        .width(Length::Fill)
        .into()
}

struct StatusDotCanvas {
    active: bool,
}

impl<M> widget::canvas::Program<M, Theme, cosmic::Renderer> for StatusDotCanvas {
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
        let center = Point::new(bounds.width / 2.0, bounds.height / 2.0);
        let radius = bounds.width.min(bounds.height) / 2.0;
        let color = if self.active {
            SOUNDCORE_GREEN
        } else {
            Color::from_rgb(0.5, 0.5, 0.5)
        };
        frame.fill(&widget::canvas::Path::circle(center, radius), color);
        vec![frame.into_geometry()]
    }
}
