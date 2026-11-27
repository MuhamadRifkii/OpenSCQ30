use std::borrow::Cow;

use cosmic::{Element, iced::core::text::Wrapping, widget};

pub fn information<'a, M>(name: Cow<'a, str>, text: Cow<'a, str>, on_copy: M) -> Element<'a, M>
where
    M: Clone + 'static,
{
    widget::settings::item::builder(name)
        // The copy button being with the title rather than the value isn't ideal, but putting it with the value causes
        // layout issues. If those layout issues are ever fixed, remove this comment and replace the flex control with
        // a row containing text and the icon button.
        .icon(
            widget::button::custom(crate::icons::tinted_secondary(crate::icons::copy(), 16))
                .class(crate::theme::transparent_row_button_style(10.0))
                .padding(6)
                .on_press(on_copy),
        )
        .flex_control(widget::text(text).wrapping(Wrapping::WordOrGlyph))
        .into()
}
