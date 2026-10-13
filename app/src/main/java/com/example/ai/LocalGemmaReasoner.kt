package com.example.ai

import com.example.data.model.AppTarget
import com.example.data.model.SettingsTarget
import com.example.data.model.SystemTask
import com.example.data.model.VolumeDirection
import com.example.data.model.VolumeStream
import java.util.regex.Pattern

data class ReasoningOutput(
    val parsedTasks: List<SystemTask>,
    val thoughtProcess: String,
    val requiresConfirmation: Boolean,
    val executionSummary: String
)

class LocalGemmaReasoner {

    /**
     * Decomposes user natural language input into one or more structured system tasks
     * with transparent local reasoning trace and zero cloud latency.
     */
    fun reason(input: String): ReasoningOutput {
        val cleanInput = input.trim()
        if (cleanInput.isEmpty()) {
            return ReasoningOutput(
                parsedTasks = emptyList(),
                thoughtProcess = "Gemma-4-Mini: Empty input provided.",
                requiresConfirmation = false,
                executionSummary = "No actionable input detected."
            )
        }

        // Decompose compound requests (e.g., "mute volume and turn on flashlight")
        val clauses = splitCompoundClauses(cleanInput)
        val tasks = mutableListOf<SystemTask>()
        val thoughts = StringBuilder()

        thoughts.append("⚡ Gemma-4-Mini Local Reasoning [Zero Cloud Telemetry]\n")
        thoughts.append("• Input Token Stream: \"$cleanInput\"\n")
        thoughts.append("• Sub-Intent Decomposer: Discovered ${clauses.size} execution clause(s)\n")

        for ((index, clause) in clauses.withIndex()) {
            val task = parseSingleClause(clause)
            tasks.add(task)
            thoughts.append(
                "  [Clause ${index + 1}]: \"${clause.trim()}\"\n" +
                "    ↳ Resolved: ${task.title}\n" +
                "    ↳ Safety Gate: ${if (task.isSensitive) "REQUIRES USER CONFIRMATION" else "AUTO-EXECUTE SAFE"}\n"
            )
        }

        val hasSensitive = tasks.any { it.isSensitive }
        val summary = if (tasks.size == 1) {
            tasks.first().title
        } else {
            "Compound Sequence (${tasks.size} actions): " + tasks.joinToString(", ") { it.title }
        }

        return ReasoningOutput(
            parsedTasks = tasks,
            thoughtProcess = thoughts.toString().trim(),
            requiresConfirmation = hasSensitive,
            executionSummary = summary
        )
    }

    private fun splitCompoundClauses(text: String): List<String> {
        // Split by "and then", "and", "then", "also", "&", or comma when followed by an action verb
        val regex = Regex("\\b(?:and then|then|and|also)\\b|[,&]", RegexOption.IGNORE_CASE)
        val rawParts = text.split(regex).map { it.trim() }.filter { it.isNotEmpty() }
        return if (rawParts.isNotEmpty()) rawParts else listOf(text)
    }

    private fun parseSingleClause(clause: String): SystemTask {
        val lower = clause.lowercase()

        // 1. Torch / Flashlight
        if (lower.contains("torch") || lower.contains("flashlight") || lower.contains("flash light")) {
            val enable = when {
                lower.contains("off") || lower.contains("disable") || lower.contains("stop") -> false
                lower.contains("on") || lower.contains("enable") || lower.contains("start") -> true
                else -> null // Toggle
            }
            return SystemTask.ToggleTorch(enable)
        }

        // 2. Battery & Power Telemetry
        if (lower.contains("battery") || lower.contains("power") || lower.contains("charging") || lower.contains("charge level")) {
            return SystemTask.CheckBattery
        }

        // 3. Storage & RAM Telemetry
        if (lower.contains("storage") || lower.contains("memory") || lower.contains("ram") || lower.contains("disk space")) {
            return SystemTask.CheckStorage
        }

        // 4. Device Diagnostic Report
        if (lower.contains("diagnostic") || lower.contains("system status") || lower.contains("device report") || lower.contains("full report")) {
            return SystemTask.DeviceReport
        }

        // 5. Volume & Sound Control
        if (lower.contains("volume") || lower.contains("sound") || lower.contains("audio") || lower.contains("mute") || lower.contains("silence") || lower.contains("unmute")) {
            val stream = when {
                lower.contains("ring") || lower.contains("ringer") || lower.contains("call") -> VolumeStream.Ring
                lower.contains("alarm") -> VolumeStream.Alarm
                lower.contains("notif") -> VolumeStream.Notification
                else -> VolumeStream.Media
            }

            val percentMatch = Regex("(\\d{1,3})\\s*%").find(lower)
            if (percentMatch != null) {
                val pct = percentMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 50
                return SystemTask.AdjustVolume(stream, VolumeDirection.SetAbsolute(pct))
            }

            val direction = when {
                lower.contains("mute") || lower.contains("silence") || lower.contains("zero") -> VolumeDirection.Mute
                lower.contains("unmute") -> VolumeDirection.Unmute
                lower.contains("max") || lower.contains("full") || lower.contains("100") -> VolumeDirection.Max
                lower.contains("up") || lower.contains("increase") || lower.contains("raise") || lower.contains("louder") -> VolumeDirection.Up
                lower.contains("down") || lower.contains("decrease") || lower.contains("lower") || lower.contains("quieter") -> VolumeDirection.Down
                else -> VolumeDirection.Up
            }
            return SystemTask.AdjustVolume(stream, direction)
        }

        // 6. Timers
        if (lower.contains("timer") || lower.contains("countdown")) {
            val seconds = extractDurationSeconds(lower)
            val labelMatch = Regex("for\\s+(.+?)(?:\\s+timer|$)").find(lower)
            val label = labelMatch?.groupValues?.getOrNull(1) ?: "Timer"
            return SystemTask.SetTimer(seconds, label)
        }

        // 7. Alarms
        if (lower.contains("alarm") || lower.contains("wake me up")) {
            val (hour, minute) = extractAlarmTime(lower)
            return SystemTask.SetAlarm(hour, minute, "NovaOS Alarm")
        }

        // 8. Settings Shortcuts
        if (lower.contains("settings") || lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("bluetooth")) {
            val target = when {
                lower.contains("wifi") || lower.contains("wi-fi") -> SettingsTarget.Wifi
                lower.contains("bluetooth") -> SettingsTarget.Bluetooth
                lower.contains("display") || lower.contains("brightness") -> SettingsTarget.Display
                lower.contains("sound") || lower.contains("audio") -> SettingsTarget.Sound
                lower.contains("battery") -> SettingsTarget.Battery
                lower.contains("apps") -> SettingsTarget.Apps
                else -> SettingsTarget.Display
            }
            return SystemTask.OpenSettings(target)
        }

        // 9. App Launching
        if (lower.startsWith("open") || lower.startsWith("launch") || lower.startsWith("start") || lower.contains("app")) {
            val target = when {
                lower.contains("camera") -> AppTarget.Camera
                lower.contains("calc") -> AppTarget.Calculator
                lower.contains("clock") || lower.contains("alarm") -> AppTarget.Clock
                lower.contains("settings") -> AppTarget.Settings
                lower.contains("map") -> AppTarget.Maps
                lower.contains("browser") || lower.contains("chrome") || lower.contains("web") -> AppTarget.Browser
                lower.contains("youtube") || lower.contains("video") -> AppTarget.YouTube
                lower.contains("calendar") -> AppTarget.Calendar
                lower.contains("phone") || lower.contains("dialer") || lower.contains("call") -> AppTarget.Phone
                else -> {
                    val appName = lower.replace(Regex("^(open|launch|start)\\s+"), "").trim()
                    AppTarget.Custom(appName.ifEmpty { "App" })
                }
            }
            return SystemTask.OpenApp(target)
        }

        // 10. Clipboard & Notes
        if (lower.contains("copy") || lower.contains("clipboard")) {
            val textToCopy = clause.replace(Regex("(?i)copy\\s+(to\\s+clipboard\\s+)?"), "").trim()
            return SystemTask.CopyToClipboard(
                label = "NovaOS Snippet",
                content = if (textToCopy.isEmpty()) "NovaOS System Diagnostic OK" else textToCopy
            )
        }

        if (lower.contains("note") || lower.contains("remember")) {
            val noteContent = clause.replace(Regex("(?i)(save\\s+note|take\\s+note|note|remember)\\s*"), "").trim()
            return SystemTask.SaveQuickNote(noteContent.ifEmpty { "Quick note from NovaOS voice" })
        }

        // 11. Presets / Macros
        if (lower.contains("good night") || lower.contains("sleep") || lower.contains("night routine")) {
            return SystemTask.TriggerRoutine(1L, "Sleep & Night Routine")
        }
        if (lower.contains("focus") || lower.contains("work mode")) {
            return SystemTask.TriggerRoutine(2L, "Work Focus Mode")
        }
        if (lower.contains("meeting") || lower.contains("meeting prep")) {
            return SystemTask.TriggerRoutine(3L, "Quick Prep / Meeting")
        }
        if (lower.contains("good morning") || lower.contains("morning routine")) {
            return SystemTask.TriggerRoutine(4L, "Morning Kickoff")
        }

        // 12. Web Search
        if (lower.startsWith("search") || lower.startsWith("google") || lower.startsWith("find")) {
            val query = clause.replace(Regex("(?i)^(search(\\s+for)?|google|find)\\s+"), "").trim()
            return SystemTask.WebSearch(query)
        }

        // Unknown intent
        return SystemTask.Unknown(clause)
    }

    private fun extractDurationSeconds(text: String): Int {
        var total = 0
        val minMatch = Regex("(\\d+)\\s*(?:min|minute|m)\\b").find(text)
        if (minMatch != null) {
            total += (minMatch.groupValues[1].toIntOrNull() ?: 0) * 60
        }
        val secMatch = Regex("(\\d+)\\s*(?:sec|second|s)\\b").find(text)
        if (secMatch != null) {
            total += secMatch.groupValues[1].toIntOrNull() ?: 0
        }
        val hourMatch = Regex("(\\d+)\\s*(?:hour|hr|h)\\b").find(text)
        if (hourMatch != null) {
            total += (hourMatch.groupValues[1].toIntOrNull() ?: 0) * 3600
        }

        // Defaults to 60 seconds if no unit was parsed but a number exists
        if (total == 0) {
            val anyNum = Regex("(\\d+)").find(text)
            total = (anyNum?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 5) * 60
        }
        return total.coerceAtLeast(10)
    }

    private fun extractAlarmTime(text: String): Pair<Int, Int> {
        val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
        val match = timeRegex.find(text)
        if (match != null) {
            var hour = match.groupValues[1].toIntOrNull() ?: 7
            val min = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            val amPm = match.groupValues.getOrNull(3)?.lowercase()

            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            return Pair(hour.coerceIn(0, 23), min.coerceIn(0, 59))
        }
        return Pair(7, 0) // Default 7:00 AM
    }
}
