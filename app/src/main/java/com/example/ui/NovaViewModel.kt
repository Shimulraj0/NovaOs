package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LocalGemmaReasoner
import com.example.ai.ReasoningOutput
import com.example.data.db.CommandLogEntity
import com.example.data.db.NovaDatabase
import com.example.data.db.RoutineEntity
import com.example.data.model.AppTarget
import com.example.data.model.SettingsTarget
import com.example.data.model.SystemTask
import com.example.data.model.VolumeDirection
import com.example.data.model.VolumeStream
import com.example.data.repository.NovaRepository
import com.example.system.ExecutionResult
import com.example.system.SystemTaskExecutor
import com.example.system.SystemTelemetrySnapshot
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NovaTab(val title: String) {
    CONSOLE("Console"),
    CONTROLS("System Tasks"),
    ROUTINES("Routines"),
    TELEMETRY("Telemetry")
}

data class PendingAction(
    val prompt: String,
    val tasks: List<SystemTask>,
    val reasoning: String,
    val logId: Long
)

data class NovaUiState(
    val inputText: String = "",
    val isListening: Boolean = false,
    val audioRms: Float = 0f,
    val isTtsEnabled: Boolean = true,
    val activeTab: NovaTab = NovaTab.CONSOLE,
    val pendingAction: PendingAction? = null,
    val lastReasoning: String? = null,
    val lastVocalFeedback: String? = null,
    val statusMessage: String = "NovaOS Neural Core Online • 100% On-Device",
    val isTorchActive: Boolean = false,
    val telemetry: SystemTelemetrySnapshot? = null
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NovaRepository
    private val localReasoner = LocalGemmaReasoner()
    private val taskExecutor = SystemTaskExecutor(application)
    private val ttsManager = TtsManager(application)
    private var speechRecognizerHelper: SpeechRecognizerHelper? = null

    private val _uiState = MutableStateFlow(NovaUiState())
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()

    val commandLogs: StateFlow<List<CommandLogEntity>>
    val routines: StateFlow<List<RoutineEntity>>

    init {
        val database = NovaDatabase.getDatabase(application, viewModelScope)
        repository = NovaRepository(database.novaDao())

        commandLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        routines = repository.allRoutines.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize Speech Recognizer Helper
        speechRecognizerHelper = SpeechRecognizerHelper(
            context = application,
            onFinalResult = { transcribed ->
                _uiState.value = _uiState.value.copy(inputText = transcribed)
                submitCommand(transcribed)
            }
        )

        // Bind Speech Flow
        viewModelScope.launch {
            speechRecognizerHelper?.isListening?.collect { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        }
        viewModelScope.launch {
            speechRecognizerHelper?.audioRmsDb?.collect { rms ->
                _uiState.value = _uiState.value.copy(audioRms = rms)
            }
        }
        viewModelScope.launch {
            speechRecognizerHelper?.partialText?.collect { partial ->
                if (partial.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(inputText = partial)
                }
            }
        }

        refreshTelemetry()
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setActiveTab(tab: NovaTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
        if (tab == NovaTab.TELEMETRY) {
            refreshTelemetry()
        }
    }

    fun toggleTts() {
        val enabled = ttsManager.toggleTts()
        _uiState.value = _uiState.value.copy(isTtsEnabled = enabled)
    }

    fun toggleVoiceListening() {
        val currentlyListening = _uiState.value.isListening
        if (currentlyListening) {
            speechRecognizerHelper?.stopListening()
        } else {
            speechRecognizerHelper?.startListening()
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch(Dispatchers.IO) {
            val snap = taskExecutor.getTelemetry()
            _uiState.value = _uiState.value.copy(
                telemetry = snap,
                isTorchActive = snap.isTorchOn
            )
        }
    }

    fun submitCommand(queryText: String) {
        val query = queryText.trim()
        if (query.isEmpty()) return

        // Clear input bar
        _uiState.value = _uiState.value.copy(inputText = "")

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val reasoningOutput: ReasoningOutput = localReasoner.reason(query)
            val latency = System.currentTimeMillis() - startTime

            if (reasoningOutput.requiresConfirmation) {
                // Log as confirmation required
                val logId = repository.logCommand(
                    prompt = query,
                    taskType = "SENSITIVE_CONFIRMATION",
                    summary = reasoningOutput.executionSummary,
                    status = "PENDING_CONFIRMATION",
                    latencyMs = latency,
                    reasoning = reasoningOutput.thoughtProcess
                )

                _uiState.value = _uiState.value.copy(
                    pendingAction = PendingAction(
                        prompt = query,
                        tasks = reasoningOutput.parsedTasks,
                        reasoning = reasoningOutput.thoughtProcess,
                        logId = logId
                    ),
                    lastReasoning = reasoningOutput.thoughtProcess,
                    statusMessage = "Confirmation Required: ${reasoningOutput.executionSummary}"
                )

                ttsManager.speak("Confirmation required for ${reasoningOutput.executionSummary}")
            } else {
                // Auto-execute safe action sequence
                executeTasksDirectly(
                    prompt = query,
                    tasks = reasoningOutput.parsedTasks,
                    reasoning = reasoningOutput.thoughtProcess,
                    latencyMs = latency
                )
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingAction ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<ExecutionResult>()
            for (task in pending.tasks) {
                val res = taskExecutor.execute(task)
                results.add(res)
            }

            val allSuccess = results.all { it.success }
            val vocalText = results.joinToString(". ") { it.vocalFeedback }
            val detailsText = results.joinToString("\n") { it.details }

            repository.updateLogStatus(pending.logId, if (allSuccess) "SUCCESS" else "PARTIAL_FAILURE")

            _uiState.value = _uiState.value.copy(
                pendingAction = null,
                lastVocalFeedback = vocalText,
                statusMessage = "Executed: ${pending.prompt}"
            )

            ttsManager.speak(vocalText)
            refreshTelemetry()
        }
    }

    fun cancelPendingAction() {
        val pending = _uiState.value.pendingAction ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLogStatus(pending.logId, "CANCELLED")
            _uiState.value = _uiState.value.copy(
                pendingAction = null,
                statusMessage = "Action Cancelled: ${pending.prompt}"
            )
            ttsManager.speak("Action cancelled")
        }
    }

    private suspend fun executeTasksDirectly(
        prompt: String,
        tasks: List<SystemTask>,
        reasoning: String,
        latencyMs: Long
    ) {
        val results = mutableListOf<ExecutionResult>()
        for (task in tasks) {
            val res = taskExecutor.execute(task)
            results.add(res)
        }

        val allSuccess = results.all { it.success }
        val vocalText = results.joinToString(". ") { it.vocalFeedback }
        val detailsSummary = results.joinToString(" • ") { it.details }

        repository.logCommand(
            prompt = prompt,
            taskType = if (tasks.size > 1) "COMPOUND_ROUTINE" else tasks.firstOrNull()?.iconKey ?: "GENERIC",
            summary = detailsSummary,
            status = if (allSuccess) "SUCCESS" else "FAILED",
            latencyMs = latencyMs,
            reasoning = reasoning
        )

        _uiState.value = _uiState.value.copy(
            lastReasoning = reasoning,
            lastVocalFeedback = vocalText,
            statusMessage = vocalText
        )

        ttsManager.speak(vocalText)
        refreshTelemetry()
    }

    // Direct Quick-Actions from Controls Grid
    fun triggerTorch() {
        submitCommand("toggle torch")
    }

    fun triggerVolume(direction: VolumeDirection) {
        when (direction) {
            is VolumeDirection.Up -> submitCommand("volume up")
            is VolumeDirection.Down -> submitCommand("volume down")
            is VolumeDirection.Mute -> submitCommand("mute volume")
            is VolumeDirection.Unmute -> submitCommand("unmute volume")
            is VolumeDirection.Max -> submitCommand("set volume to 100%")
            is VolumeDirection.SetAbsolute -> submitCommand("set volume to ${direction.percent}%")
        }
    }

    fun triggerBatteryReport() {
        submitCommand("check battery")
    }

    fun triggerStorageReport() {
        submitCommand("check storage")
    }

    fun triggerFullDiagnostic() {
        submitCommand("system status")
    }

    fun triggerAppLaunch(target: AppTarget) {
        submitCommand("open ${target.displayName}")
    }

    fun triggerSettings(target: SettingsTarget) {
        submitCommand("open ${target.displayName}")
    }

    fun triggerRoutine(routine: RoutineEntity) {
        submitCommand(routine.triggerPhrase)
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(
                lastReasoning = null,
                statusMessage = "Command console history cleared"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper?.stopListening()
        ttsManager.shutdown()
    }
}
