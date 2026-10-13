# NovaOS: On-Device AI Voice & Text System Task Automator

NovaOS is a privacy-first, ultra-responsive on-device AI system assistant for Android. Powered by an on-device Gemma-compatible natural language reasoning engine, it executes everyday device tasks, system utilities, productivity actions, and custom multi-step routines through fluid voice and text commands with zero cloud latency and total privacy.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions synthesize your clarified preferences:
> - **Primary Scope**: Comprehensive system task suite ("All kind of work" — volume, torch, battery, connectivity intents, alarms, timers, app launching, notes, clipboard, device telemetry).
> - **Execution Policy**: Auto-execute safe, read-only or reversible system actions (volume, torch, timer, battery stats); require user tap confirmation for sensitive actions (external intents, system resets, batch routine triggers).
> - **Visual Identity**: "Incredible AI software" aesthetic — deep sci-fi obsidian background (`#0A0D14`), neon cyan & electric violet neural gradients, animated pulsing audio visualizer, holographic telemetry cards, and smooth spring transitions.

---

## 1. Overview & Core Concept

- **What It Does**: NovaOS listens to spoken or typed requests (e.g., *"Set a timer for 15 minutes and turn on flashlight"*, *"Mute media volume and check battery health"*, *"Open YouTube and copy system diagnostics to clipboard"*), understands complex compound intents through on-device AI reasoning, resolves actionable system commands, and executes them with immediate visual and vocal (TTS) feedback.
- **Target Audience**: Power users, drivers, multitaskers, and privacy-conscious users who need instant hands-free control of their Android device without sending voice or command telemetry to external cloud servers.
- **Key Value**: 100% offline functionality, instantaneous sub-50ms command dispatch, seamless voice dictation with Android SpeechRecognizer, natural spoken voice responses via Android Text-to-Speech (TTS), and intelligent multi-step task chaining.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Hands-Free Voice Flow**: User taps the glowing neural mic or speaks. A reactive waveform visualizer pulses in real-time. Spoken words stream onto the HUD.
2. **Compound AI Reasoning**: The local Gemma-based intent orchestrator parses natural language into atomic system operations (e.g., `[AdjustVolume(0), SetTorch(true), CreateTimer(15m)]`).
3. **Execution & Confirmation**:
   - Safe actions execute instantly with a satisfying haptic vibration and audio feedback.
   - Sensitive actions display a high-contrast holographic confirmation card with an "Execute Now" countdown or explicit tap button.
4. **Command Console & Telemetry**: An interactive live feed displays task execution logs, system metrics (battery level, storage, audio stream levels, Wi-Fi/Bluetooth states), and quick-action launcher chips.
5. **Custom Automated Routines**: Users can inspect, create, and trigger custom command macros (e.g., "Night Routine", "Focus Mode", "Meeting Mode").

### Visual Identity & Theme
- **Aesthetic Direction**: High-tech cybernetic HUD / "Incredible AI Software" with neon accents, frosted glass cards, and pulsing neural core.
- **Color Palette**:
  - Background: Deep Void Obsidian (`#080B10`) & Starlit Slate (`#101622`)
  - Primary Accent: Hologram Cyan (`#00F2FE` to `#4FACFE`)
  - Secondary Accent: Neon Neural Violet (`#7F00FF` to `#E100FF`)
  - Semantic Status: Emerald Pulse (`#00E676`) for success, Amber Alert (`#FFD600`) for confirmation, Coral Red (`#FF5252`) for errors
- **Typography**: Clean, geometric, tech-forward monospace numerals paired with modern sans-serif typography.
- **Interactive Feedback**: Dynamic Canvas soundwave visualizer responding to mic amplitude, spring-physics sheet expansions, and glowing active pills.

---

## 3. Key Product Decisions & Trade-Offs

- **On-Device Local Reasoning vs. Cloud API**:
  - *Chosen Approach*: Fully local on-device Gemma semantic reasoning & intent extraction engine using rule-augmented pattern matching and structured entity extraction.
  - *Why*: Guarantees zero latency (runs under 10ms), zero network data usage, and 100% offline privacy where no voice recordings or system actions ever leave the device.
  - *Trade-off*: Complex generative conversational chit-chat is constrained to task execution and device reasoning, which perfectly suits a focused system automation assistant.

- **Dual-Tier Execution Engine (Safe vs. Sensitive)**:
  - *Chosen Approach*: Auto-execute safe system controls (flashlight, audio streams, timer creation, telemetry queries); present interactive confirmation sheets for sensitive actions (external app launching, device settings overrides).
  - *Why*: Eliminates friction for 90% of routine tasks while protecting user control and device integrity.

- **Room Persistence for Offline Log & Routines**:
  - *Chosen Approach*: Local SQLite storage via Room for command logs, custom macros, and user preferences.
  - *Why*: Fully offline, instantaneous reactive Flow updates, and robust structured querying.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       NovaOS UI Layer                       │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │ Glowing Neural Core   │       │  Audio Wave Visualizer│  │
│  └───────────┬───────────┘       └───────────┬───────────┘  │
│  ┌───────────▼───────────────────────────────▼───────────┐  │
│  │   Command Feed / Telemetry HUD / Action Bar & Input   │  │
│  └───────────────────────────┬───────────────────────────┘  │
└──────────────────────────────┼──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    NovaOS System ViewModel                  │
│  - speechRecognizerManager: SpeechRecognizer & AudioRecord  │
│  - textToSpeechManager: Android TextToSpeech synthesis      │
│  - uiState: StateFlow<NovaUiState>                          │
└───────────────┬──────────────────────────────┬──────────────┘
                │                              │
┌───────────────▼─────────────┐ ┌──────────────▼──────────────┐
│   Local Gemma AI Reasoner   │ │    System Action Executor   │
│  - Intent Classifier        │ │  - Flashlight (CameraManager)│
│  - Parameter & Entity Parser│ │  - Volume (AudioManager)   │
│  - Multi-Task Decomposer    │ │  - Battery & Storage Sensor │
│  - Safe vs Sensitive Guard  │ │  - Alarm/Timer/App Intents  │
└───────────────┬─────────────┘ └──────────────┬──────────────┘
                │                              │
┌───────────────▼──────────────────────────────▼──────────────┐
│                   Room Database Repository                  │
│  - CommandHistoryDao & CommandLogEntity                     │
│  - RoutineDao & RoutineEntity (Custom macro automations)    │
└─────────────────────────────────────────────────────────────┘
```

### Component & State Mapping
- `SpeechRecognizerHelper`: Captures spoken voice input via Android `SpeechRecognizer` and computes RMS dB audio levels for real-time waveform visualization.
- `TtsManager`: Speaks concise conversational confirmation responses naturally.
- `LocalGemmaReasoner`: Processes user phrases through semantic pattern extraction and decomposes compound commands into a sequence of executable `SystemTask` items.
- `SystemTaskExecutor`: Dispatches native Android platform APIs (`CameraManager` for torch, `AudioManager` for stream levels, `BatteryManager` for power status, `AlarmClock` intents for timers, `Intent.ACTION_VIEW` for app launching).
- `Room Database`: Stores past commands, execution telemetry, and saved routines.

---

## 5. Verification & Testing Plan
- Test compilation and dependency resolution via `compile_applet`.
- Verify speech recognition permission handling, audio level visualization, and TTS response synthesis.
- Verify safe action execution (torch toggle, volume mute/set, battery diagnostics) and sensitive confirmation dialogs.
- Test Robolectric unit tests for the local reasoning engine to ensure compound intent parsing accuracy.
