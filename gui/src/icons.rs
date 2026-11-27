use cosmic::{
    Element, Theme,
    iced::widget::svg,
    theme,
    widget::{self, icon::Handle},
};

use crate::theme::SoundcorePalette;

fn svg_handle(bytes: &'static [u8]) -> Handle {
    widget::icon::from_svg_bytes(bytes).symbolic(true)
}

fn tinted_icon<'a, M: 'a>(
    handle: Handle,
    size: u16,
    use_secondary: bool,
) -> Element<'a, M> {
    widget::icon::icon(handle)
        .size(size)
        .class(theme::Svg::custom(move |theme: &Theme| {
            let p = SoundcorePalette::from_theme(theme);
            svg::Style {
                color: Some(if use_secondary {
                    p.text_secondary
                } else {
                    p.text_primary
                }),
            }
        }))
        .into()
}

pub fn tinted_accent<'a, M: 'a>(handle: Handle, size: u16) -> Element<'a, M> {
    widget::icon::icon(handle)
        .size(size)
        .class(theme::Svg::custom(|theme: &Theme| {
            let p = SoundcorePalette::from_theme(theme);
            svg::Style {
                color: Some(p.accent),
            }
        }))
        .into()
}

pub fn tinted_on_accent<'a, M: 'a>(handle: Handle, size: u16) -> Element<'a, M> {
    widget::icon::icon(handle)
        .size(size)
        .class(theme::Svg::custom(|theme: &Theme| {
            let p = SoundcorePalette::from_theme(theme);
            svg::Style {
                color: Some(p.on_accent),
            }
        }))
        .into()
}

pub fn tinted_primary<'a, M: 'a>(handle: Handle, size: u16) -> Element<'a, M> {
    tinted_icon(handle, size, false)
}

pub fn tinted_secondary<'a, M: 'a>(handle: Handle, size: u16) -> Element<'a, M> {
    tinted_icon(handle, size, true)
}

pub fn openscq30() -> Handle {
    widget::icon::from_svg_bytes(include_bytes!(
        "../resources/com.oppzippy.OpenSCQ30.svg"
    ))
    .symbolic(false)
}

pub fn arrow_back() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M313,520L509,716Q521,728 520.5,744Q520,760 508,772Q496,783 480,783.5"##,
            r##"Q464,784 452,772L188,508Q182,502 179.5,495Q177,488 177,480Q177,472 179.5,465"##,
            r##"Q182,458 188,452L452,188Q463,177 479.5,177Q496,177 508,188Q520,200 520,216.5"##,
            r##"Q520,233 508,245L313,440L760,440Q777,440 788.5,451.5Q800,463 800,480Q800,497 "##,
            r##"788.5,508.5Q777,520 760,520L313,520Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn chevron_right() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn chevron_right_tinted<'a, M: 'a>(size: u16) -> Element<'a, M> {
    tinted_secondary(chevron_right(), size)
}

pub fn settings() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M433,880Q406,880 386.5,862Q367,844 363,818L354,752Q341,747 329.5,740"##,
            r##"Q318,733 307,725L245,751Q220,762 195,753Q170,744 156,721L109,639Q95,616 "##,
            r##"101,590Q107,564 128,547L181,507Q180,500 180,493.5Q180,487 180,480Q180,473 "##,
            r##"180,466.5Q180,460 181,453L128,413Q107,396 101,370Q95,344 109,321L156,239"##,
            r##"Q170,216 195,207Q220,198 245,209L307,235Q318,227 330,220Q342,213 354,208"##,
            r##"L363,142Q367,116 386.5,98Q406,80 433,80L527,80Q554,80 573.5,98Q593,116 "##,
            r##"597,142L606,208Q619,213 630.5,220Q642,227 653,235L715,209Q740,198 765,207"##,
            r##"Q790,216 804,239L851,321Q865,344 859,370Q853,396 832,413L779,453Q780,460 "##,
            r##"780,466.5Q780,473 780,480Q780,487 780,493.5Q780,500 778,507L831,547Q852,564 "##,
            r##"858,590Q864,616 850,639L802,721Q788,744 763,753Q738,762 713,751L653,725"##,
            r##"Q642,733 630,740Q618,747 606,752L597,818Q593,844 573.5,862Q554,880 527,880"##,
            r##"L433,880ZM482,620Q540,620 581,579Q622,538 622,480Q622,422 581,381Q540,340 "##,
            r##"482,340Q423,340 382.5,381Q342,422 342,480Q342,538 382.5,579Q423,620 "##,
            r##"482,620Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn info() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M508.5,668.5Q520,657 520,640L520,480Q520,463 508.5,451.5Q497,440 "##,
            r##"480,440Q463,440 451.5,451.5Q440,463 440,480L440,640Q440,657 451.5,668.5"##,
            r##"Q463,680 480,680Q497,680 508.5,668.5ZM508.5,348.5Q520,337 520,320Q520,303 "##,
            r##"508.5,291.5Q497,280 480,280Q463,280 451.5,291.5Q440,303 440,320Q440,337 "##,
            r##"451.5,348.5Q463,360 480,360Q497,360 508.5,348.5ZM480,880Q397,880 324,848.5"##,
            r##"Q251,817 197,763Q143,709 111.5,636Q80,563 80,480Q80,397 111.5,324Q143,251 "##,
            r##"197,197Q251,143 324,111.5Q397,80 480,80Q563,80 636,111.5Q709,143 763,197"##,
            r##"Q817,251 848.5,324Q880,397 880,480Q880,563 848.5,636Q817,709 763,763"##,
            r##"Q709,817 636,848.5Q563,880 480,880Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn graphic_eq() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M3,13h2v-2H3v2zm0,4h2v-2H3v2zm0,-8h2V7H3v2zm4,8h2V7H7v10z"##,
            r##"m4,4h2V3h-2v18zm4,-4h2V7h-2v10zm4,-4h2v-2h-2v2z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn equalizer() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M183.5,776.5Q160,753 160,720L160,560Q160,527 183.5,503.5Q207,480 "##,
            r##"240,480Q273,480 296.5,503.5Q320,527 320,560L320,720Q320,753 296.5,776.5"##,
            r##"Q273,800 240,800Q207,800 183.5,776.5ZM480,800Q447,800 423.5,776.5Q400,753 "##,
            r##"400,720L400,240Q400,207 423.5,183.5Q447,160 480,160Q513,160 536.5,183.5"##,
            r##"Q560,207 560,240L560,720Q560,753 536.5,776.5Q513,800 480,800ZM663.5,776.5"##,
            r##"Q640,753 640,720L640,440Q640,407 663.5,383.5Q687,360 720,360Q753,360 "##,
            r##"776.5,383.5Q800,407 800,440L800,720Q800,753 776.5,776.5Q753,800 720,800"##,
            r##"Q687,800 663.5,776.5Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn noise_control() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M12,1c-4.97,0 -9,4.03 -9,9v7c0,1.66 1.34,3 3,3h3v-8H5v-2"##,
            r##"c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v2h-4v8h3c1.66,0 3,-1.34 3,-3v-7"##,
            r##"c0,-4.97 -4.03,-9 -9,-9z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn person() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 "##,
            r##"1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 "##,
            r##"-8,-4z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn speaker() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M680,880L280,880Q247,880 223.5,856.5Q200,833 200,800L200,160"##,
            r##"Q200,127 223.5,103.5Q247,80 280,80L680,80Q713,80 736.5,103.5Q760,127 "##,
            r##"760,160L760,800Q760,833 736.5,856.5Q713,880 680,880ZM480,360Q513,360 "##,
            r##"536.5,336.5Q560,313 560,280Q560,247 536.5,223.5Q513,200 480,200Q447,200 "##,
            r##"423.5,223.5Q400,247 400,280Q400,313 423.5,336.5Q447,360 480,360ZM480,760"##,
            r##"Q546,760 593,713Q640,666 640,600Q640,534 593,487Q546,440 480,440Q414,440 "##,
            r##"367,487Q320,534 320,600Q320,666 367,713Q414,760 480,760ZM480,680Q447,680 "##,
            r##"423.5,656.5Q400,633 400,600Q400,567 423.5,543.5Q447,520 480,520Q513,520 "##,
            r##"536.5,543.5Q560,567 560,600Q560,633 536.5,656.5Q513,680 480,680Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn bolt() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M360,600L236,600Q212,600 200.5,578.5Q189,557 203,537L502,107"##,
            r##"Q512,93 528,87.5Q544,82 561,88Q578,94 586,109Q594,124 592,141L560,400"##,
            r##"L715,400Q741,400 751.5,423Q762,446 745,466L416,860Q405,873 389,877"##,
            r##"Q373,881 358,874Q343,867 334.5,852.5Q326,838 328,821L360,600Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn check() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M382,606L721,267Q733,255 749,255Q765,255 777,267Q789,279 789,295.5"##,
            r##"Q789,312 777,324L410,692Q398,704 382,704Q366,704 354,692L182,520Q170,508 "##,
            r##"170.5,491.5Q171,475 183,463Q195,451 211.5,451Q228,451 240,463L382,606Z"/>"##,
            r##"</svg>"##,
        )
        .as_bytes(),
    )
}

pub fn add() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M440,520L240,520Q223,520 211.5,508.5Q200,497 200,480Q200,463 "##,
            r##"211.5,451.5Q223,440 240,440L440,440L440,240Q440,223 451.5,211.5Q463,200 "##,
            r##"480,200Q497,200 508.5,211.5Q520,223 520,240L520,440L720,440Q737,440 "##,
            r##"748.5,451.5Q760,463 760,480Q760,497 748.5,508.5Q737,520 720,520L520,520"##,
            r##"L520,720Q520,737 508.5,748.5Q497,760 480,760Q463,760 451.5,748.5"##,
            r##"Q440,737 440,720L440,520Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn remove() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 960 960" fill="#ffffff">"##,
            r##"<path d="M240,520Q223,520 211.5,508.5Q200,497 200,480Q200,463 211.5,451.5"##,
            r##"Q223,440 240,440L720,440Q737,440 748.5,451.5Q760,463 760,480Q760,497 "##,
            r##"748.5,508.5Q737,520 720,520L240,520Z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn copy() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M16,1H4c-1.1,0 -2,0.9 -2,2v14h2V3h12V1zm3,4H8c-1.1,0 -2,0.9 "##,
            r##"-2,2v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7c0,-1.1 -0.9,-2 "##,
            r##"-2,-2zm0,16H8V7h11v14z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn close() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 "##,
            r##"6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn bluetooth() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 "##,
            r##"6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83"##,
            r##"l1.88,1.88L13,9.59V5.83zm1.88,10.46L13,18.17v-3.76l1.88,1.88z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn help() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 "##,
            r##"12,2zm1,17h-2v-2h2v2zm2.07,-7.75l-0.9,0.92C13.45,12.9 13,13.5 13,15h-2"##,
            r##"v-0.5c0,-1.1 0.45,-2.1 1.17,-2.83l1.24,-1.26c0.37,-0.36 0.59,-0.86 "##,
            r##"0.59,-1.41 0,-1.1 -0.9,-2 -2,-2s-2,0.9 -2,2H8c0,-2.21 1.79,-4 4,-4s4,"##,
            r##"1.79 4,4c0,0.88 -0.36,1.68 -0.93,2.25z"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn radio_checked() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10"##,
            r##"S17.52,2 12,2zm0,18c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 "##,
            r##"8,8 -3.58,8 -8,8z"/><circle cx="12" cy="12" r="5"/></svg>"##,
        )
        .as_bytes(),
    )
}

pub fn refresh() -> Handle {
    svg_handle(
        concat!(
            r##"<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ffffff">"##,
            r##"<path d="M17.65,6.35A7.958,7.958 0,0 0,12 4c-4.42,0 -7.99,3.58 "##,
            r##"-7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08A5.99,5.99 "##,
            r##"0,0 1,12 18c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 "##,
            r##"4.22,1.78L13,11h7V4l-2.35,2.35z"/></svg>"##,
        )
        .as_bytes(),
    )
}
