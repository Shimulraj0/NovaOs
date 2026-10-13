package com.example.data.model

sealed class VolumeStream(val nameId: String) {
    object Media : VolumeStream("Media")
    object Ring : VolumeStream("Ring")
    object Alarm : VolumeStream("Alarm")
    object Notification : VolumeStream("Notification")
}

sealed class VolumeDirection {
    object Up : VolumeDirection()
    object Down : VolumeDirection()
    object Mute : VolumeDirection()
    object Unmute : VolumeDirection()
    object Max : VolumeDirection()
    data class SetAbsolute(val percent: Int) : VolumeDirection()
}

sealed class AppTarget(val displayName: String, val packageNameHint: String) {
    object Camera : AppTarget("Camera", "camera")
    object Calculator : AppTarget("Calculator", "calculator")
    object Clock : AppTarget("Clock / Alarms", "deskclock")
    object Settings : AppTarget("Settings", "settings")
    object Maps : AppTarget("Maps", "maps")
    object Browser : AppTarget("Web Browser", "chrome")
    object YouTube : AppTarget("YouTube", "youtube")
    object Calendar : AppTarget("Calendar", "calendar")
    object Phone : AppTarget("Phone / Dialer", "dialer")
    data class Custom(val appName: String) : AppTarget(appName, "")
}

sealed class SettingsTarget(val displayName: String) {
    object Wifi : SettingsTarget("Wi-Fi Settings")
    object Bluetooth : SettingsTarget("Bluetooth Settings")
    object Display : SettingsTarget("Display & Brightness")
    object Sound : SettingsTarget("Sound & Haptics")
    object Battery : SettingsTarget("Battery Optimization")
    object Apps : SettingsTarget("Installed Apps")
}

sealed class SystemTask(
    val title: String,
    val description: String,
    val isSensitive: Boolean,
    val iconKey: String
) {
    data class ToggleTorch(val enable: Boolean?) : SystemTask(
        title = if (enable == true) "Turn On Torch" else if (enable == false) "Turn Off Torch" else "Toggle Torch",
        description = "Controls the device rear camera LED flashlight",
        isSensitive = false,
        iconKey = "flashlight"
    )

    data class AdjustVolume(
        val stream: VolumeStream = VolumeStream.Media,
        val direction: VolumeDirection
    ) : SystemTask(
        title = when (direction) {
            is VolumeDirection.Mute -> "Mute ${stream.nameId} Volume"
            is VolumeDirection.Unmute -> "Unmute ${stream.nameId} Volume"
            is VolumeDirection.Max -> "Max ${stream.nameId} Volume"
            is VolumeDirection.Up -> "Increase ${stream.nameId} Volume"
            is VolumeDirection.Down -> "Decrease ${stream.nameId} Volume"
            is VolumeDirection.SetAbsolute -> "Set ${stream.nameId} to ${direction.percent}%"
        },
        description = "Adjusts audio stream output levels",
        isSensitive = false,
        iconKey = "volume"
    )

    object CheckBattery : SystemTask(
        title = "Inspect Battery Telemetry",
        description = "Reads battery percentage, charging current, and thermal state",
        isSensitive = false,
        iconKey = "battery"
    )

    object CheckStorage : SystemTask(
        title = "Inspect System Storage & RAM",
        description = "Queries available internal memory and RAM capacity",
        isSensitive = false,
        iconKey = "storage"
    )

    data class SetTimer(val durationSeconds: Int, val label: String = "Timer") : SystemTask(
        title = "Set Timer: ${formatSeconds(durationSeconds)}",
        description = "Creates countdown timer for $label",
        isSensitive = false,
        iconKey = "timer"
    )

    data class SetAlarm(val hour: Int, val minute: Int, val message: String = "Alarm") : SystemTask(
        title = "Set Alarm at %02d:%02d".format(hour, minute),
        description = "Programs system clock alarm for $message",
        isSensitive = false,
        iconKey = "alarm"
    )

    data class OpenApp(val target: AppTarget, val query: String? = null) : SystemTask(
        title = "Launch ${target.displayName}",
        description = "Switches foreground activity to ${target.displayName}",
        isSensitive = true, // external application launch requires confirmation per policy
        iconKey = "app"
    )

    data class OpenSettings(val target: SettingsTarget) : SystemTask(
        title = "Open ${target.displayName}",
        description = "Opens system settings screen",
        isSensitive = true, // opening system settings
        iconKey = "settings"
    )

    data class CopyToClipboard(val label: String, val content: String) : SystemTask(
        title = "Copy to Clipboard",
        description = "Copies '$content' to system clipboard",
        isSensitive = false,
        iconKey = "clipboard"
    )

    data class SaveQuickNote(val noteText: String) : SystemTask(
        title = "Save Quick Note",
        description = "Stores note securely in local SQLite storage",
        isSensitive = false,
        iconKey = "note"
    )

    data class WebSearch(val searchQuery: String) : SystemTask(
        title = "Search Web: '$searchQuery'",
        description = "Launches search query in default web browser",
        isSensitive = true,
        iconKey = "search"
    )

    data class TriggerRoutine(val routineId: Long, val routineName: String) : SystemTask(
        title = "Execute Routine: $routineName",
        description = "Runs multi-step macro automation sequence",
        isSensitive = true, // multi-task macro routine confirmation
        iconKey = "routine"
    )

    object DeviceReport : SystemTask(
        title = "Generate System Diagnostic Report",
        description = "Compiles full device hardware, battery, and memory state",
        isSensitive = false,
        iconKey = "report"
    )

    data class Unknown(val rawInput: String) : SystemTask(
        title = "Unrecognized System Command",
        description = "Could not map '$rawInput' to known system task",
        isSensitive = false,
        iconKey = "help"
    )

    companion object {
        fun formatSeconds(totalSecs: Int): String {
            val mins = totalSecs / 60
            val secs = totalSecs % 60
            return when {
                mins > 0 && secs > 0 -> "$mins min $secs sec"
                mins > 0 -> "$mins min"
                else -> "$secs sec"
            }
        }
    }
}
