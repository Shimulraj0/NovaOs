package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.RoutineEntity
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VoidBlack

@Composable
fun RoutinesSection(
    routines: List<RoutineEntity>,
    onTriggerRoutine: (RoutineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("routines_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUTOMATED MACRO ROUTINES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = VioletNeon,
                letterSpacing = 1.sp
            )
            Text(
                text = "Compound Tasks",
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(routines, key = { it.id }) { routine ->
                RoutineCard(
                    routine = routine,
                    onExecute = { onTriggerRoutine(routine) }
                )
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onExecute: () -> Unit
) {
    val iconVector: ImageVector = when (routine.icon) {
        "bedtime" -> Icons.Filled.Bedtime
        "psychology" -> Icons.Filled.Psychology
        "groups" -> Icons.Filled.Groups
        "wb_sunny" -> Icons.Filled.WbSunny
        else -> Icons.Filled.Psychology
    }

    val iconColor = when (routine.icon) {
        "bedtime" -> VioletNeon
        "psychology" -> CyanNeon
        "groups" -> EmeraldPulse
        "wb_sunny" -> Color(0xFFFFD600)
        else -> CyanNeon
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("routine_card_${routine.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = routine.title,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = routine.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Say: \"${routine.triggerPhrase}\"",
                            fontSize = 11.sp,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = onExecute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor,
                        contentColor = VoidBlack
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("run_routine_btn_${routine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Run",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Run",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routine.description,
                fontSize = 12.sp,
                color = TextSecondaryDark,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Actions: ${routine.taskTypesJson.split(',').size} chained steps",
                    fontSize = 11.sp,
                    color = TextMutedDark,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
