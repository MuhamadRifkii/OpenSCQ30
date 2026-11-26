use std::{
    sync::{
        Arc, Mutex,
        atomic::{AtomicBool, AtomicU8, Ordering},
    },
    time::Duration,
};

use openscq30_lib_has::Has;
use strum::{EnumIter, EnumString};
use tokio::sync::watch;

use crate::{
    devices::soundcore::common::{
        modules::{
            ModuleCollection, limit_high_volume::state_modifier::LimitHighVolumeStateModifier,
        },
        packet::{self, PacketIOController},
        structures::{DecibelReadingRefreshRate, LimitHighVolume},
    },
    macros::enum_subset,
    settings::{CategoryId, SettingId},
};

mod setting_handler;
mod state_modifier;

enum_subset! {
    SettingId,
    #[derive(EnumIter, EnumString)]
    enum LimitHighVolumeSetting {
        LimitHighVolume,
        LimitHighVolumeDbLimit,
        LimitHighVolumeRefreshRate,
        LimitHighVolumeDecibelMonitoring,
        LimitHighVolumeCurrentDb,
    }
}

pub struct LimitHighVolumeLiveState {
    pub pending_monitoring: Mutex<Option<bool>>,
    pub monitoring_enabled: AtomicBool,
    pub current_db: AtomicU8,
    pub monitoring_notify: watch::Sender<bool>,
    pub change_notify: watch::Sender<()>,
}

impl LimitHighVolumeLiveState {
    pub fn new(change_notify: watch::Sender<()>) -> Self {
        let (monitoring_notify, _) = watch::channel(false);
        Self {
            pending_monitoring: Mutex::new(None),
            monitoring_enabled: AtomicBool::new(false),
            current_db: AtomicU8::new(0),
            monitoring_notify,
            change_notify,
        }
    }
}

fn poll_interval(refresh_rate: DecibelReadingRefreshRate) -> Duration {
    match refresh_rate {
        DecibelReadingRefreshRate::RealTime => Duration::from_secs(1),
        DecibelReadingRefreshRate::TenSeconds => Duration::from_secs(10),
        DecibelReadingRefreshRate::OneMinute => Duration::from_secs(60),
    }
}

impl<T> ModuleCollection<T>
where
    T: Has<LimitHighVolume> + Clone + Send + Sync + 'static,
{
    pub fn add_limit_high_volume(
        &mut self,
        packet_io: Arc<PacketIOController>,
        mut state_rx: watch::Receiver<T>,
        change_notify: watch::Sender<()>,
    ) {
        let live_state = Arc::new(LimitHighVolumeLiveState::new(change_notify));
        let mut monitoring_rx = live_state.monitoring_notify.subscribe();
        let poll_packet_io = packet_io.clone();
        let poll_live_state = live_state.clone();

        tokio::spawn(async move {
            loop {
                if !*monitoring_rx.borrow_and_update() {
                    tokio::select! {
                        res = monitoring_rx.changed() => {
                            if res.is_err() {
                                break;
                            }
                        }
                        res = state_rx.changed() => {
                            if res.is_err() {
                                break;
                            }
                        }
                    }
                    continue;
                }

                match poll_packet_io
                    .send_with_response(&packet::outbound::request_limit_high_volume_current_db())
                    .await
                {
                    Ok(response) => {
                        let db = response.body.first().copied().unwrap_or(0);
                        if poll_live_state.current_db.swap(db, Ordering::SeqCst) != db {
                            let _ = poll_live_state.change_notify.send(());
                        }
                    }
                    Err(err) => {
                        tracing::debug!("failed to poll current decibel reading: {err:?}");
                    }
                }

                let current_refresh_rate = state_rx.borrow_and_update().get().refresh_rate;
                let sleep = tokio::time::sleep(poll_interval(current_refresh_rate));
                tokio::pin!(sleep);
                loop {
                    tokio::select! {
                        () = &mut sleep => break,
                        res = monitoring_rx.changed() => {
                            if res.is_err() {
                                return;
                            }
                            break;
                        }
                        res = state_rx.changed() => {
                            if res.is_err() {
                                return;
                            }
                            if state_rx.borrow_and_update().get().refresh_rate != current_refresh_rate {
                                break;
                            }
                        }
                    }
                }
            }
        });

        self.setting_manager.add_handler(
            CategoryId::LimitHighVolume,
            setting_handler::LimitHighVolumeSettingHandler::new(live_state.clone()),
        );
        self.state_modifiers
            .push(Box::new(LimitHighVolumeStateModifier::new(
                packet_io,
                live_state,
            )));
    }
}
