package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelInfo
import com.example.ui.components.CyberHudCard
import com.example.ui.theme.BullGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurfaceBorder
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AiSwarmScreen(
    models: List<AiModelInfo>,
    selectedModelId: String,
    isSwarmConsensusMode: Boolean,
    geminiSignal: com.example.data.ai.GeminiProtocolSignal? = null,
    isGeminiAnalyzing: Boolean = false,
    onRunGeminiAnalysis: () -> Unit = {},
    onExecuteTrade: (com.example.data.model.Direction, Double) -> Unit = { _, _ -> },
    onSelectModel: (String) -> Unit,
    onToggleSwarmConsensus: (Boolean) -> Unit,
    onSaveApiKey: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI SWARM INTELLIGENCE",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "15 Hybrid AI Models • Multi-Consensus Engine",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Text(
                        text = "15/15 ONLINE",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Swarm Multi-Consensus Switch Card
        item {
            CyberHudCard(
                glowColor = NeonCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HYBRID SWARM MULTI-CONSENSUS",
                            color = NeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Combines predictions from all 15 models. Trades only when >= 85% models reach consensus.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSwarmConsensusMode,
                        onCheckedChange = onToggleSwarmConsensus,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = TerminalSurfaceBorder
                        ),
                        modifier = Modifier.testTag("swarm_consensus_switch")
                    )
                }
            }
        }

        // Gemini AI Standard Trading Protocol Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRunGeminiAnalysis,
                    enabled = !isGeminiAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("ai_swarm_gemini_analysis_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isGeminiAnalyzing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI PROTOCOL REASONING...",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ RUN GEMINI QUANT PROTOCOL ANALYSIS",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                if (geminiSignal != null) {
                    com.example.ui.components.GeminiProtocolSignalCard(
                        signal = geminiSignal,
                        onExecute = {
                            val dir = if (geminiSignal.direction.equals("BUY", ignoreCase = true)) com.example.data.model.Direction.UP else com.example.data.model.Direction.DOWN
                            onExecuteTrade(dir, 10.0)
                        }
                    )
                }
            }
        }

        // Section Title: 15 Integrated AI Engines
        item {
            Text(
                text = "15 INTEGRATED AI ENGINES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp, top = 4.dp)
            )
        }

        // Models List
        items(models) { model ->
            val isSelected = model.id == selectedModelId
            var enteredKey by remember(model.apiKey) { mutableStateOf(model.apiKey) }
            var isSaved by remember { mutableStateOf(false) }

            CyberHudCard(
                borderColor = if (isSelected) NeonCyan else TerminalSurfaceBorder,
                glowColor = if (isSelected) NeonCyan else Color.Transparent
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = model.name,
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                if (model.isRecommended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NeonGold.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED",
                                            color = NeonGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${model.provider} • ${model.specialty}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Accuracy & Latency
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${model.accuracyScore}%",
                                color = BullGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${model.latencyMs}ms",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // API Key Manual Input & Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = enteredKey,
                            onValueChange = {
                                enteredKey = it
                                isSaved = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("api_key_input_${model.id}"),
                            placeholder = { Text("Configure custom API key...", color = TextSecondary, fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TerminalSurfaceBorder,
                                focusedContainerColor = TerminalSurfaceElevated,
                                unfocusedContainerColor = TerminalSurfaceElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                onSaveApiKey(model.id, enteredKey)
                                isSaved = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaved) BullGreen else TerminalSurfaceElevated
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("save_api_key_${model.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Save,
                                contentDescription = null,
                                tint = if (isSaved) Color.Black else NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSaved) "Saved" else "Save",
                                color = if (isSaved) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Select Lead Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonCyan.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACTIVE LEAD ENGINE",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerminalSurfaceElevated,
                                modifier = Modifier.clickable { onSelectModel(model.id) }
                            ) {
                                Text(
                                    text = "Select As Lead",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
