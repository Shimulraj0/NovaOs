package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.SystemTelemetrySnapshot
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletNeon

@Composable
fun TelemetryHud(
    telemetry: SystemTelemetrySnapshot?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snap = telemetry ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("telemetry_hud"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Row: Battery Dial & Storage Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Battery Hologram Card
            TelemetryGlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { snap.batteryPercent / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (snap.isCharging) EmeraldPulse else CyanNeon,
                            trackColor = BorderDark,
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Battery charge state",
                            tint = if (snap.isCharging) EmeraldPulse else CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${snap.batteryPercent}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (snap.isCharging) "Charging • ${"%.1f".format(snap.batteryTemperatureC)}°C" else "${snap.batteryHealth} • ${"%.1f".format(snap.batteryTemperatureC)}°C",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            // Storage Hologram Card
            TelemetryGlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SdStorage,
                                contentDescription = "Device Storage",
                                tint = VioletNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Storage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryDark
                            )
                        }
                        Text(
                            text = "${"%.1f".format(snap.freeStorageGb)} GB Free",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val storageFraction = ((snap.totalStorageGb - snap.freeStorageGb) / snap.totalStorageGb).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { storageFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = VioletNeon,
                        trackColor = BorderDark,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total ${"%.0f".format(snap.totalStorageGb)} GB",
                        fontSize = 10.sp,
                        color = TextMutedDark
                    )
                }
            }
        }

        // Bottom Row: RAM Memory & Audio Level Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RAM Pill
            TelemetryGlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = "Device RAM",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RAM Usage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${snap.usedRamMb} MB / ${snap.totalRamMb} MB",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val ramFraction = if (snap.totalRamMb > 0) (snap.usedRamMb.toFloat() / snap.totalRamMb).coerceIn(0f, 1f) else 0.5f
                    LinearProgressIndicator(
                        progress = { ramFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = CyanNeon,
                        trackColor = BorderDark,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Audio & Torch Status
            TelemetryGlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Media Audio Volume",
                                tint = EmeraldPulse,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Media Vol",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryDark
                            )
                        }
                        Text(
                            text = "${snap.mediaVolumePercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FlashlightOn,
                                contentDescription = "Flashlight state",
                                tint = if (snap.isTorchOn) AmberAlertState else TextMutedDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Torch LED",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                        Text(
                            text = if (snap.isTorchOn) "ACTIVE" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (snap.isTorchOn) AmberAlertState else TextMutedDark,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

val AmberAlertState = Color(0xFFFFD600)

@Composable
fun TelemetryGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        content()
    }
}
