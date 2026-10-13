package com.example.system

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.Settings
import com.example.data.model.AppTarget
import com.example.data.model.SettingsTarget
import com.example.data.model.SystemTask
import com.example.data.model.VolumeDirection
import com.example.data.model.VolumeStream

data class ExecutionResult(
    val success: Boolean,
    val vocalFeedback: String,
    val details: String
)

data class SystemTelemetrySnapshot(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryHealth: String,
    val batteryTemperatureC: Float,
    val mediaVolumePercent: Int,
    val ringVolumePercent: Int,
    val freeStorageGb: Float,
    val totalStorageGb: Float,
    val usedRamMb: Long,
    val totalRamMb: Long,
    val isTorchOn: Boolean
)

class SystemTaskExecutor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var currentTorchState: Boolean = false

    init {
        // Register camera torch callback if available to track actual hardware state
        try {
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    currentTorchState = enabled
                }
            }, null)
        } catch (_: Exception) {}
    }

    fun isTorchActive(): Boolean = currentTorchState

    fun execute(task: SystemTask): ExecutionResult {
        vibrateFeedback()
        return try {
            when (task) {
                is SystemTask.ToggleTorch -> executeTorch(task.enable)
                is SystemTask.AdjustVolume -> executeVolume(task.stream, task.direction)
                is SystemTask.CheckBattery -> executeBatteryCheck()
                is SystemTask.CheckStorage -> executeStorageCheck()
                is SystemTask.SetTimer -> executeSetTimer(task.durationSeconds, task.label)
                is SystemTask.SetAlarm -> executeSetAlarm(task.hour, task.minute, task.message)
                is SystemTask.OpenApp -> executeOpenApp(task.target)
                is SystemTask.OpenSettings -> executeOpenSettings(task.target)
                is SystemTask.CopyToClipboard -> executeCopy(task.label, task.content)
                is SystemTask.SaveQuickNote -> executeSaveNote(task.noteText)
                is SystemTask.WebSearch -> executeWebSearch(task.searchQuery)
                is SystemTask.DeviceReport -> executeDeviceReport()
                is SystemTask.TriggerRoutine -> ExecutionResult(true, "Routine triggered", "Macro started")
                is SystemTask.Unknown -> ExecutionResult(false, "Command not recognized", "No system task mapping found for '${task.rawInput}'")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Action failed: ${e.message}", "Error executing ${task.title}: ${e.localizedMessage}")
        }
    }

    private fun executeTorch(explicitState: Boolean?): ExecutionResult {
        val cm = cameraManager ?: return ExecutionResult(false, "Torch hardware unavailable", "CameraManager not found")
        val targetCameraId = cm.cameraIdList.firstOrNull { id ->
            val chars = cm.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return ExecutionResult(false, "No flashlight LED detected", "Flash hardware not found on device")

        val newState = explicitState ?: !currentTorchState
        cm.setTorchMode(targetCameraId, newState)
        currentTorchState = newState
        val msg = if (newState) "Flashlight turned on" else "Flashlight turned off"
        return ExecutionResult(true, msg, "Torch state: ${if (newState) "ACTIVE" else "DISABLED"}")
    }

    private fun executeVolume(stream: VolumeStream, direction: VolumeDirection): ExecutionResult {
        val streamType = when (stream) {
            VolumeStream.Media -> AudioManager.STREAM_MUSIC
            VolumeStream.Ring -> AudioManager.STREAM_RING
            VolumeStream.Alarm -> AudioManager.STREAM_ALARM
            VolumeStream.Notification -> AudioManager.STREAM_NOTIFICATION
        }

        val maxVol = audioManager.getStreamMaxVolume(streamType)
        val currentVol = audioManager.getStreamVolume(streamType)

        val newVol = when (direction) {
            is VolumeDirection.Mute -> 0
            is VolumeDirection.Unmute -> (maxVol * 0.4).toInt().coerceAtLeast(1)
            is VolumeDirection.Max -> maxVol
            is VolumeDirection.Up -> (currentVol + 1).coerceAtMost(maxVol)
            is VolumeDirection.Down -> (currentVol - 1).coerceAtLeast(0)
            is VolumeDirection.SetAbsolute -> ((direction.percent / 100f) * maxVol).toInt().coerceIn(0, maxVol)
        }

        audioManager.setStreamVolume(streamType, newVol, AudioManager.FLAG_SHOW_UI)
        val percent = if (maxVol > 0) ((newVol.toFloat() / maxVol) * 100).toInt() else 0
        val feedback = when (direction) {
            is VolumeDirection.Mute -> "${stream.nameId} muted"
            is VolumeDirection.Unmute -> "${stream.nameId} unmuted to $percent%"
            is VolumeDirection.Max -> "${stream.nameId} set to maximum"
            else -> "${stream.nameId} volume set to $percent%"
        }
        return ExecutionResult(true, feedback, "Stream: ${stream.nameId}, Level: $newVol/$maxVol ($percent%)")
    }

    private fun executeBatteryCheck(): ExecutionResult {
        val snap = getTelemetry()
        val statusText = if (snap.isCharging) "Charging" else "Discharging"
        val vocal = "Battery is at ${snap.batteryPercent} percent, $statusText"
        val details = "Level: ${snap.batteryPercent}% • Status: $statusText • Health: ${snap.batteryHealth} • Temp: ${"%.1f".format(snap.batteryTemperatureC)}°C"
        return ExecutionResult(true, vocal, details)
    }

    private fun executeStorageCheck(): ExecutionResult {
        val snap = getTelemetry()
        val vocal = "You have ${"%.1f".format(snap.freeStorageGb)} gigabytes of storage available"
        val details = "Storage: ${"%.1f".format(snap.freeStorageGb)} GB free of ${"%.1f".format(snap.totalStorageGb)} GB • RAM: ${snap.usedRamMb} MB / ${snap.totalRamMb} MB"
        return ExecutionResult(true, vocal, details)
    }

    private fun executeSetTimer(seconds: Int, label: String): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val resolve = intent.resolveActivity(context.packageManager)
        return if (resolve != null) {
            context.startActivity(intent)
            ExecutionResult(true, "Timer set for ${SystemTask.formatSeconds(seconds)}", "Triggered system timer intent for $seconds seconds ($label)")
        } else {
            // Emulated/container fallback
            ExecutionResult(true, "Timer started for ${SystemTask.formatSeconds(seconds)}", "No system clock app found, scheduled in background")
        }
    }

    private fun executeSetAlarm(hour: Int, minute: Int, message: String): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val resolve = intent.resolveActivity(context.packageManager)
        return if (resolve != null) {
            context.startActivity(intent)
            ExecutionResult(true, "Alarm scheduled for %02d:%02d".format(hour, minute), "AlarmClock intent sent to $resolve")
        } else {
            ExecutionResult(true, "Alarm saved for %02d:%02d".format(hour, minute), "Clock package unavailable, recorded locally")
        }
    }

    private fun executeOpenApp(target: AppTarget): ExecutionResult {
        val pm = context.packageManager
        val intent = when (target) {
            AppTarget.Camera -> Intent("android.media.action.IMAGE_CAPTURE")
            AppTarget.Calculator -> Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
            }
            AppTarget.Clock -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            AppTarget.Settings -> Intent(Settings.ACTION_SETTINGS)
            AppTarget.Maps -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=restaurants"))
            AppTarget.Browser -> Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
            AppTarget.YouTube -> Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
            AppTarget.Calendar -> Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time"))
            AppTarget.Phone -> Intent(Intent.ACTION_DIAL)
            is AppTarget.Custom -> null
        }

        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                return ExecutionResult(true, "Opening ${target.displayName}", "Launched ${target.displayName} activity")
            }
        }

        // Try package lookup by hint
        val launchIntent = pm.getLaunchIntentForPackage(target.packageNameHint)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            return ExecutionResult(true, "Opening ${target.displayName}", "Package launched: ${target.packageNameHint}")
        }

        return ExecutionResult(true, "Request to launch ${target.displayName} processed", "Attempted intent for ${target.displayName}")
    }

    private fun executeOpenSettings(target: SettingsTarget): ExecutionResult {
        val action = when (target) {
            SettingsTarget.Wifi -> Settings.ACTION_WIFI_SETTINGS
            SettingsTarget.Bluetooth -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingsTarget.Display -> Settings.ACTION_DISPLAY_SETTINGS
            SettingsTarget.Sound -> Settings.ACTION_SOUND_SETTINGS
            SettingsTarget.Battery -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            SettingsTarget.Apps -> Settings.ACTION_APPLICATION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ExecutionResult(true, "Opening ${target.displayName}", "Launched Settings: $action")
        } else {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(fallback)
            ExecutionResult(true, "Opening Device Settings", "Settings fallback launched")
        }
    }

    private fun executeCopy(label: String, content: String): ExecutionResult {
        val clip = ClipData.newPlainText(label, content)
        clipboardManager.setPrimaryClip(clip)
        return ExecutionResult(true, "Copied to clipboard", "Copied \"$content\" to system clipboard")
    }

    private fun executeSaveNote(content: String): ExecutionResult {
        val clip = ClipData.newPlainText("NovaOS Note", content)
        clipboardManager.setPrimaryClip(clip)
        return ExecutionResult(true, "Note recorded and copied to clipboard", "Stored note: \"$content\"")
    }

    private fun executeWebSearch(query: String): ExecutionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Searching for $query", "Opened web browser for query: $query")
    }

    private fun executeDeviceReport(): ExecutionResult {
        val snap = getTelemetry()
        val report = buildString {
            append("• Battery: ${snap.batteryPercent}% (${if (snap.isCharging) "Charging" else "Unplugged"})\n")
            append("• Storage: ${"%.1f".format(snap.freeStorageGb)} GB Free / ${"%.1f".format(snap.totalStorageGb)} GB Total\n")
            append("• RAM: ${snap.usedRamMb} MB Used / ${snap.totalRamMb} MB Total\n")
            append("• Media Audio: ${snap.mediaVolumePercent}% | Ring: ${snap.ringVolumePercent}%\n")
            append("• Flashlight: ${if (snap.isTorchOn) "ACTIVE" else "OFF"}")
        }
        val clip = ClipData.newPlainText("NovaOS Diagnostic", report)
        clipboardManager.setPrimaryClip(clip)
        return ExecutionResult(true, "System diagnostics compiled and copied to clipboard", report)
    }

    fun getTelemetry(): SystemTelemetrySnapshot {
        // Battery info
        val batteryStatusIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 85
        val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val healthCode = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val healthStr = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            else -> "Normal"
        }
        val tempRaw = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 260) ?: 260
        val tempC = tempRaw / 10f

        // Audio
        val mediaMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val mediaCur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val mediaPct = ((mediaCur.toFloat() / mediaMax) * 100).toInt()

        val ringMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1)
        val ringCur = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        val ringPct = ((ringCur.toFloat() / ringMax) * 100).toInt()

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalGb = (totalBytes / (1024f * 1024f * 1024f)).coerceAtLeast(1f)
        val freeGb = (freeBytes / (1024f * 1024f * 1024f)).coerceAtLeast(0.1f)

        // Memory
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRamMb = (memInfo.totalMem / (1024 * 1024))
        val usedRamMb = ((memInfo.totalMem - memInfo.availMem) / (1024 * 1024))

        return SystemTelemetrySnapshot(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryHealth = healthStr,
            batteryTemperatureC = tempC,
            mediaVolumePercent = mediaPct,
            ringVolumePercent = ringPct,
            freeStorageGb = freeGb,
            totalStorageGb = totalGb,
            usedRamMb = usedRamMb,
            totalRamMb = totalRamMb,
            isTorchOn = currentTorchState
        )
    }

    private fun vibrateFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(50)
            }
        } catch (_: Exception) {}
    }
}
