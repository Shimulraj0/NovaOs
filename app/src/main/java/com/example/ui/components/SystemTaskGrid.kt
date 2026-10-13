package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.VioletNeon

data class QuickTaskItem(
    val title: String,
    val subtitle: String,
    val command: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun SystemTaskGrid(
    onTriggerCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks = listOf(
        QuickTaskItem("Torch Toggle", "Hardware LED", "toggle torch", Icons.Filled.FlashlightOn, Color(0xFFFFD600)),
        QuickTaskItem("Mute Audio", "Media Stream", "mute volume", Icons.Filled.VolumeMute, Color(0xFFFF5252)),
        QuickTaskItem("Volume Up", "+Step Audio", "volume up", Icons.Filled.VolumeUp, EmeraldPulse),
        QuickTaskItem("Volume Down", "-Step Audio", "volume down", Icons.Filled.VolumeDown, CyanNeon),
        QuickTaskItem("Timer 5m", "Quick Countdown", "set timer for 5 minutes", Icons.Filled.HourglassBottom, VioletNeon),
        QuickTaskItem("Alarm 7:00", "Morning Wakeup", "set alarm for 7 am", Icons.Filled.Alarm, EmeraldPulse),
        QuickTaskItem("Battery Info", "Power & Thermal", "check battery", Icons.Filled.BatteryFull, EmeraldPulse),
        QuickTaskItem("Memory & Disk", "Storage Telemetry", "check storage", Icons.Filled.SdStorage, VioletNeon),
        QuickTaskItem("Camera", "Capture App", "open camera", Icons.Filled.CameraAlt, CyanNeon),
        QuickTaskItem("Calculator", "System Tool", "open calculator", Icons.Filled.Calculate, Color(0xFFFF9100)),
        QuickTaskItem("Maps", "GPS & Navigation", "open maps", Icons.Filled.Map, EmeraldPulse),
        QuickTaskItem("Wi-Fi Settings", "Network Control", "open wifi settings", Icons.Filled.Wifi, CyanNeon),
        QuickTaskItem("Bluetooth", "Wireless Devices", "open bluetooth settings", Icons.Filled.Bluetooth, VioletNeon),
        QuickTaskItem("Device Settings", "OS Configuration", "open settings", Icons.Filled.Settings, TextMutedDark),
        QuickTaskItem("Copy Specs", "Diagnostic to Clipboard", "diagnostic report", Icons.Filled.ContentCopy, CyanNeon),
        QuickTaskItem("Web Search", "Google Query", "search android", Icons.Filled.Public, VioletNeon)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("system_task_grid")
    ) {
        Text(
            text = "SYSTEM CONTROLS & UTILITIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().height(440.dp)
        ) {
            items(tasks) { task ->
                TaskGridCard(
                    item = task,
                    onClick = { onTriggerCommand(task.command) }
                )
            }
        }
    }
}

@Composable
fun TaskGridCard(
    item: QuickTaskItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("grid_task_${item.title.replace(' ', '_').lowercase()}"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = TextMutedDark
                )
            }
        }
    }
}
