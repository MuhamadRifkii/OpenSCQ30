use async_trait::async_trait;
use openscq30_lib_has::Has;
use std::sync::{Arc, atomic::Ordering};
use tokio::sync::watch;

use crate::{
    api::device,
    devices::soundcore::common::{
        modules::limit_high_volume::LimitHighVolumeLiveState,
        packet::{self, PacketIOController},
        state_modifier::StateModifier,
        structures::LimitHighVolume,
    },
};

pub struct LimitHighVolumeStateModifier {
    packet_io: Arc<PacketIOController>,
    live_state: Arc<LimitHighVolumeLiveState>,
}

impl LimitHighVolumeStateModifier {
    pub fn new(
        packet_io: Arc<PacketIOController>,
        live_state: Arc<LimitHighVolumeLiveState>,
    ) -> Self {
        Self {
            packet_io,
            live_state,
        }
    }
}

#[async_trait]
impl<T> StateModifier<T> for LimitHighVolumeStateModifier
where
    T: Has<LimitHighVolume> + Clone + Send + Sync,
{
    async fn move_to_state(
        &self,
        state_sender: &watch::Sender<T>,
        target_state: &T,
    ) -> device::Result<()> {
        let target = target_state.get();
        let current = *state_sender.borrow().get();

        if current.enabled != target.enabled || current.db_limit != target.db_limit {
            self.packet_io
                .send_with_response(&packet::outbound::set_limit_high_volume(
                    target.enabled,
                    target.db_limit,
                ))
                .await?;
            state_sender.send_modify(|state| {
                state.get_mut().enabled = target.enabled;
                state.get_mut().db_limit = target.db_limit;
            });
        }

        if current.refresh_rate != target.refresh_rate {
            self.packet_io
                .send_with_response(&packet::outbound::set_limit_high_volume_refresh_rate(
                    target.refresh_rate,
                ))
                .await?;
            state_sender.send_modify(|state| {
                state.get_mut().refresh_rate = target.refresh_rate;
            });
        }

        let desired_monitoring = self.live_state.pending_monitoring.lock().unwrap().take();
        if let Some(desired) = desired_monitoring
            && desired != self.live_state.monitoring_enabled.load(Ordering::SeqCst)
        {
            self.packet_io
                .send_with_response(&packet::outbound::set_limit_high_volume_decibel_monitoring(
                    desired,
                ))
                .await?;
            self.live_state
                .monitoring_enabled
                .store(desired, Ordering::SeqCst);
            if !desired {
                self.live_state.current_db.store(0, Ordering::SeqCst);
            }
            let _ = self.live_state.monitoring_notify.send(desired);
            let _ = self.live_state.change_notify.send(());
        }

        Ok(())
    }
}
