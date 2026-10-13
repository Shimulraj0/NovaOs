package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CommandConsole
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.NeuralOrbVisualizer
import com.example.ui.components.RoutinesSection
import com.example.ui.components.SystemTaskGrid
import com.example.ui.components.TelemetryHud
import com.example.ui.components.VoiceInputBar
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VoidBlack

@Composable
fun NovaMainScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs by viewModel.commandLogs.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Audio recording permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        }
    }

    fun handleMicTap() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().background(VoidBlack),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            VoiceInputBar(
                inputText = uiState.inputText,
                isListening = uiState.isListening,
                isTtsEnabled = uiState.isTtsEnabled,
                onInputChanged = viewModel::onInputTextChanged,
                onSubmit = viewModel::submitCommand,
                onToggleMic = ::handleMicTap,
                onToggleTts = viewModel::toggleTts
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VoidBlack, ObsidianDark, Color(0xFF090E18))
                    )
                )
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isListening) EmeraldPulse else CyanNeon)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NOVAOS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = TextPrimaryDark,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "GEMMA LOCAL AI • OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Torch Quick Toggle
                    IconButton(
                        onClick = viewModel::triggerTorch,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, CircleShape)
                            .testTag("torch_top_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isTorchActive) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                            contentDescription = "Torch",
                            tint = if (uiState.isTorchActive) Color(0xFFFFD600) else TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Telemetry Refresh
                    IconButton(
                        onClick = viewModel::refreshTelemetry,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, CircleShape)
                            .testTag("refresh_telemetry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Neural Core & Audio Visualizer Section
            NeuralOrbVisualizer(
                isListening = uiState.isListening,
                audioRms = uiState.audioRms,
                statusText = uiState.statusMessage,
                onClick = ::handleMicTap,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )

            // Dynamic Assistant Status Pill
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark.copy(alpha = 0.8f))
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.statusMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (uiState.isListening) EmeraldPulse else TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = CyanNeon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                        height = 2.5.dp,
                        color = CyanNeon
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDark))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                NovaTab.values().forEach { tab ->
                    val isSelected = uiState.activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanNeon else TextSecondaryDark
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area based on Selected Tab
            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "TabContentTransition"
            ) { activeTab ->
                when (activeTab) {
                    NovaTab.CONSOLE -> {
                        CommandConsole(
                            logs = logs,
                            lastReasoning = uiState.lastReasoning,
                            onQuickPromptClick = viewModel::submitCommand,
                            onClearHistory = viewModel::clearHistory,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NovaTab.CONTROLS -> {
                        SystemTaskGrid(
                            onTriggerCommand = viewModel::submitCommand,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NovaTab.ROUTINES -> {
                        RoutinesSection(
                            routines = routines,
                            onTriggerRoutine = viewModel::triggerRoutine,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NovaTab.TELEMETRY -> {
                        TelemetryHud(
                            telemetry = uiState.telemetry,
                            onRefresh = viewModel::refreshTelemetry,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Sensitive Action Confirmation Dialog
        uiState.pendingAction?.let { pending ->
            ConfirmationDialog(
                pendingAction = pending,
                onConfirm = viewModel::confirmPendingAction,
                onCancel = viewModel::cancelPendingAction
            )
        }
    }
}
