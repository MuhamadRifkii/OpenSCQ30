use std::sync::{Arc, atomic::Ordering};

use async_trait::async_trait;
use openscq30_lib_has::Has;
use strum::IntoEnumIterator;

use crate::{
    api::settings::{Setting, SettingId, Value},
    devices::soundcore::common::{
        modules::limit_high_volume::{LimitHighVolumeLiveState, LimitHighVolumeSetting},
        settings_manager::{SettingHandler, SettingHandlerError, SettingHandlerResult},
        structures::LimitHighVolume,
    },
    settings,
};

pub struct LimitHighVolumeSettingHandler {
    live_state: Arc<LimitHighVolumeLiveState>,
}

impl LimitHighVolumeSettingHandler {
    pub fn new(live_state: Arc<LimitHighVolumeLiveState>) -> Self {
        Self { live_state }
    }
}

#[async_trait]
impl<T> SettingHandler<T> for LimitHighVolumeSettingHandler
where
    T: Has<LimitHighVolume> + Send,
{
    fn settings(&self) -> Vec<SettingId> {
        LimitHighVolumeSetting::iter().map(Into::into).collect()
    }

    fn get(&self, state: &T, setting_id: &SettingId) -> Option<Setting> {
        let limit_high_volume = state.get();
        self.get_inner(limit_high_volume, setting_id)
    }

    async fn set(
        &self,
        state: &mut T,
        setting_id: &SettingId,
        value: Value,
    ) -> SettingHandlerResult<()> {
        let limit_high_volume = state.get_mut();
        self.set_inner(limit_high_volume, setting_id, value)
    }
}

impl LimitHighVolumeSettingHandler {
    #[inline(never)]
    fn get_inner(
        &self,
        limit_high_volume: &LimitHighVolume,
        setting_id: &SettingId,
    ) -> Option<Setting> {
        let setting: LimitHighVolumeSetting = (*setting_id).try_into().ok()?;
        Some(match setting {
            LimitHighVolumeSetting::LimitHighVolume => Setting::Toggle {
                value: limit_high_volume.enabled,
            },
            LimitHighVolumeSetting::LimitHighVolumeDbLimit => Setting::I32Range {
                setting: settings::Range {
                    range: 75..=100,
                    step: 5,
                },
                value: limit_high_volume.db_limit.into(),
            },
            LimitHighVolumeSetting::LimitHighVolumeRefreshRate => {
                Setting::select_from_enum_all_variants(limit_high_volume.refresh_rate)
            }
            LimitHighVolumeSetting::LimitHighVolumeDecibelMonitoring => Setting::Toggle {
                value: self.live_state.monitoring_enabled.load(Ordering::SeqCst),
            },
            LimitHighVolumeSetting::LimitHighVolumeCurrentDb => {
                let db = self.live_state.current_db.load(Ordering::SeqCst);
                Setting::Information {
                    value: db.to_string(),
                    translated_value: format!("{db} dB"),
                }
            }
        })
    }

    #[inline(never)]
    fn set_inner(
        &self,
        limit_high_volume: &mut LimitHighVolume,
        setting_id: &SettingId,
        value: Value,
    ) -> SettingHandlerResult<()> {
        let setting: LimitHighVolumeSetting = (*setting_id)
            .try_into()
            .expect("already filtered to valid values only by SettingsManager");
        match setting {
            LimitHighVolumeSetting::LimitHighVolume => {
                limit_high_volume.enabled = value.try_as_bool()?;
            }
            LimitHighVolumeSetting::LimitHighVolumeDbLimit => {
                let db_limit = value.try_as_i32()?;
                // Only multiples of 5 are allowed
                let clamped_db_limit = (db_limit - (db_limit % 5)).clamp(75, 100);
                limit_high_volume.db_limit = clamped_db_limit as u8;
            }
            LimitHighVolumeSetting::LimitHighVolumeRefreshRate => {
                limit_high_volume.refresh_rate = value.try_as_enum_variant()?;
            }
            LimitHighVolumeSetting::LimitHighVolumeDecibelMonitoring => {
                *self.live_state.pending_monitoring.lock().unwrap() = Some(value.try_as_bool()?);
            }
            LimitHighVolumeSetting::LimitHighVolumeCurrentDb => {
                return Err(SettingHandlerError::ReadOnly);
            }
        }
        Ok(())
    }
}
