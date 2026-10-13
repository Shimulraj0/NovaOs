package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.VioletNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeuralOrbVisualizer(
    isListening: Boolean,
    audioRms: Float,
    statusText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuralOrbAnimation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 600 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(190.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("neural_orb_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2.7f
            val audioBoost = (audioRms * 28f).coerceAtLeast(0f)
            val dynamicRadius = (baseRadius * (if (isListening) pulsePhase else 1f)) + audioBoost

            // Outer Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isListening) EmeraldPulse else CyanGlow).copy(alpha = 0.28f + (audioRms * 0.35f)),
                        VioletNeon.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 1.55f
                ),
                radius = dynamicRadius * 1.55f,
                center = center
            )

            // Dynamic Audio Waveform Ring
            val waveSegments = 32
            val angleStep = (2 * PI / waveSegments).toFloat()
            for (i in 0 until waveSegments) {
                val theta = i * angleStep + (rotationAngle * PI.toFloat() / 180f)
                val waveOffset = sin(theta * 4f + rotationAngle * 0.05f) * (6f + audioRms * 18f)
                val r1 = dynamicRadius + waveOffset
                val r2 = dynamicRadius + 14f + (waveOffset * 1.5f)

                val start = Offset(
                    center.x + r1 * cos(theta),
                    center.y + r1 * sin(theta)
                )
                val end = Offset(
                    center.x + r2 * cos(theta),
                    center.y + r2 * sin(theta)
                )

                val ringColor = if (i % 2 == 0) CyanNeon else VioletNeon
                drawLine(
                    color = ringColor.copy(alpha = 0.75f + (audioRms * 0.25f)),
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Orbital Tracker Rings
            drawCircle(
                color = CyanNeon.copy(alpha = 0.45f),
                radius = dynamicRadius * 1.08f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            drawCircle(
                color = VioletNeon.copy(alpha = 0.35f),
                radius = dynamicRadius * 0.88f,
                center = center,
                style = Stroke(width = 1.8f)
            )

            // Inner Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        if (isListening) EmeraldPulse else CyanNeon,
                        VioletNeon.copy(alpha = 0.85f),
                        Color(0xFF0F172A)
                    ),
                    center = center,
                    radius = dynamicRadius * 0.7f
                ),
                radius = dynamicRadius * 0.7f,
                center = center
            )
        }

        // Center Icon
        Icon(
            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.Psychology,
            contentDescription = if (isListening) "Listening microphone active" else "NovaOS Intelligence Core",
            tint = Color.White,
            modifier = Modifier.size(38.dp)
        )
    }
}
