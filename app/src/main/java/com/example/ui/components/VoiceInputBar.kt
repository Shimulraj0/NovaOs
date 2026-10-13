package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VoidBlack

@Composable
fun VoiceInputBar(
    inputText: String,
    isListening: Boolean,
    isTtsEnabled: Boolean,
    onInputChanged: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onToggleMic: () -> Unit,
    onToggleTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianDark)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(BorderDark, Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .testTag("voice_input_bar")
    ) {
        // Listening status banner if active
        AnimatedVisibility(visible = isListening) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldPulse.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldPulse)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Listening... Speak your command clearly",
                    fontSize = 11.sp,
                    color = EmeraldPulse,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TTS Voice Toggle Button
            IconButton(
                onClick = onToggleTts,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isTtsEnabled) CyanNeon.copy(alpha = 0.15f) else SurfaceDark)
                    .border(1.dp, if (isTtsEnabled) CyanNeon.copy(alpha = 0.4f) else BorderDark, CircleShape)
                    .testTag("tts_toggle_button")
            ) {
                Icon(
                    imageVector = if (isTtsEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = if (isTtsEnabled) "TTS voice response active" else "TTS muted",
                    tint = if (isTtsEnabled) CyanNeon else TextMutedDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Command Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Listening to voice..." else "Command or question...",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    cursorColor = CyanNeon
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) onSubmit(inputText)
                })
            )

            // Submit Button (Send or Mic FAB)
            if (inputText.isNotBlank()) {
                IconButton(
                    onClick = { onSubmit(inputText) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CyanNeon)
                        .testTag("submit_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send Command",
                        tint = VoidBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Mic Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.radialGradient(listOf(EmeraldPulse, CyanNeon))
                            else Brush.radialGradient(listOf(CyanNeon, VioletNeon))
                        )
                        .clickable { onToggleMic() }
                        .testTag("mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = if (isListening) "Stop voice input" else "Start voice input",
                        tint = VoidBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
