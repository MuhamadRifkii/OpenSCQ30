package com.oppzippy.openscq30.ui.deviceselection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.soundcore.components.ConnectedBatteryInfo
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreBatteryStrip
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreEarbudsArtwork
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

@Composable
fun ConnectToDeviceCard(
    modifier: Modifier = Modifier,
    name: String,
    model: String,
    macAddress: String,
    isDemo: Boolean,
    isConnecting: Boolean = false,
    isConnected: Boolean = false,
    connectedBattery: ConnectedBatteryInfo? = null,
) {
    val shortName = name.removePrefix("Soundcore ").ifBlank { name }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoundcoreColors.SurfaceCard,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            if (isConnected && connectedBattery != null && connectedBattery.hasAny) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    SoundcoreBatteryStrip(
                        battery = connectedBattery,
                        compact = true,
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DeviceModelIcon(Modifier.size(80.dp), model)
                }
                Spacer(Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = shortName,
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        if (isConnecting) {
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SoundcoreColors.Cyan,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (isConnected || isConnecting || isDemo) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isConnected -> SoundcoreColors.Green
                                            isConnecting -> SoundcoreColors.Cyan
                                            else -> SoundcoreColors.Cyan
                                        },
                                    ),
                            )
                        }
                        Text(
                            text = when {
                                isConnected -> stringResource(R.string.connected)
                                isConnecting -> stringResource(R.string.connecting)
                                isDemo -> "${stringResource(R.string.demo)} • ${stringResource(R.string.not_connected)}"
                                else -> stringResource(R.string.not_connected)
                            },
                            color = when {
                                isConnected -> SoundcoreColors.TextSecondary
                                isConnecting -> SoundcoreColors.Cyan
                                else -> SoundcoreColors.TextSecondary
                            },
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddDeviceModelCard(modifier: Modifier = Modifier, name: String, model: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoundcoreColors.SurfaceCard,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoundcoreColors.SurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                DeviceModelIcon(Modifier.size(44.dp), model)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    color = SoundcoreColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = model,
                    color = SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
fun AddDeviceCard(modifier: Modifier = Modifier, name: String, macAddress: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoundcoreColors.SurfaceCard,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoundcoreColors.SurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.bluetooth_24px),
                    contentDescription = null,
                    tint = SoundcoreColors.Cyan,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    color = SoundcoreColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = macAddress,
                    color = SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun DeviceModelIcon(modifier: Modifier, model: String) {
    when (model) {
        "SoundcoreA3004",
        "SoundcoreA3005",
        "SoundcoreA3027",
        "SoundcoreA3028",
        "SoundcoreA3029",
        "SoundcoreA3030",
        "SoundcoreA3031",
        "SoundcoreA3033",
        "SoundcoreA3035",
        "SoundcoreA3040",
        "SoundcoreA3062",
        "SoundcoreD1402",
        "SoundcoreDevelopment",
        -> HeadphonesIcon(modifier)

        "SoundcoreA3116" -> SpeakerIcon(modifier)

        "SoundcoreA3959",
        "SoundcoreD1202",
        "SoundcoreD1202C",
        -> SoundcoreEarbudsArtwork(modifier = modifier, model = model)

        "SoundcoreA3330",
        "SoundcoreA3388",
        "SoundcoreA3876",
        "SoundcoreA3909",
        "SoundcoreA3926",
        "SoundcoreA3930",
        "SoundcoreA3931",
        "SoundcoreA3933",
        "SoundcoreA3935",
        "SoundcoreA3936",
        "SoundcoreA3939",
        "SoundcoreA3944",
        "SoundcoreA3945",
        "SoundcoreA3947",
        "SoundcoreA3948",
        "SoundcoreA3949",
        "SoundcoreA3951",
        "SoundcoreA3952",
        "SoundcoreA3953",
        "SoundcoreA3954",
        "SoundcoreA3955",
        "SoundcoreA3957",
        "SoundcoreA3968",
        "SoundcoreD1101",
        "SoundcoreD1301",
        -> EarbudsIcon(modifier)

        else -> HeadphonesIcon(modifier)
    }
}

@Composable
private fun HeadphonesIcon(modifier: Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.headphones_24px),
        contentDescription = stringResource(R.string.headphones),
        tint = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun EarbudsIcon(modifier: Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.earbuds_2_24px),
        contentDescription = stringResource(R.string.earbuds),
        tint = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SpeakerIcon(modifier: Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.speaker_24px),
        contentDescription = stringResource(R.string.audio_speaker),
        tint = MaterialTheme.colorScheme.onSurface,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewDeviceCard() {
    OpenSCQ30Theme(darkTheme = true, dynamicColor = false) {
        ConnectToDeviceCard(
            name = "Soundcore R60i NC",
            model = "SoundcoreD1202C",
            macAddress = "AC:12:2F:C8:6E:08",
            isDemo = false,
        )
    }
}
