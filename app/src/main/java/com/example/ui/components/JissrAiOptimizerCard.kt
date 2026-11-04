package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiOptimizationGoal
import com.example.ai.AiOptimizationUiState
import com.example.ai.AiTunnelOptimizationPlan
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrAmber
import com.example.ui.theme.JissrCrimson
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun JissrAiOptimizerCard(
    isArabic: Boolean,
    aiState: AiOptimizationUiState,
    selectedGoal: AiOptimizationGoal,
    onSelectGoal: (AiOptimizationGoal) -> Unit,
    onRunAiOptimization: () -> Unit,
    onApplyAiPlan: (AiTunnelOptimizationPlan) -> Unit,
    onStreamCustomQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customPrompt by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "aiPulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val isBusy = aiState is AiOptimizationUiState.Loading || aiState is AiOptimizationUiState.Streaming

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jissr_ai_optimizer_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    JissrEmerald.copy(alpha = auraAlpha),
                    JissrCyan.copy(alpha = 0.65f),
                    JissrAmber.copy(alpha = 0.55f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JissrEmerald.copy(alpha = 0.11f),
                            JissrCyan.copy(alpha = 0.06f),
                            ObsidianSurface
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = JissrEmerald.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_ai_neural_optimizer),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "محرك الجسر للذكاء الاصطناعي" else "Jissr AI Neural Optimizer",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (isArabic) {
                                "تحليل حي للسرعة والبنق وضبط تلقائي للنفق عبر Gemini AI"
                            } else {
                                "Live telemetry diagnosis & automatic tunnel tuning via Gemini AI"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianCardElevated,
                    border = BorderStroke(1.dp, JissrAmber.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = JissrAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "AI PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp
                            ),
                            color = JissrAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Optimization Goal Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiOptimizationGoal.entries.forEach { goal ->
                    val isSelected = goal == selectedGoal
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) JissrEmerald.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                        border = BorderStroke(
                            width = if (isSelected) 1.2.dp else 1.dp,
                            color = if (isSelected) JissrEmerald else BorderSubtleDark
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isBusy) { onSelectGoal(goal) }
                            .testTag("ai_goal_chip_${goal.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) JissrEmerald else JissrCyan.copy(alpha = 0.6f))
                            )
                            Text(
                                text = if (isArabic) goal.labelAr else goal.labelEn,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) JissrEmerald else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Primary Action Button: Run AI Smart Diagnosis & Optimization
            Button(
                onClick = onRunAiOptimization,
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("run_ai_optimization_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrEmerald,
                    contentColor = Color(0xFF032017)
                )
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.2.dp,
                        color = Color(0xFF032017)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "جاري التحليل الذكي للشبكة والخوادم..." else "Analyzing network telemetry with AI...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تحليل وتحسين الاتصال بالذكاء الاصطناعي" else "Analyze & Optimize Tunnel with AI",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Custom AI Network Advisor Input Row (Streaming)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    enabled = !isBusy,
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = if (isArabic) {
                                "اطلب تحسيناً مخصصاً (مثال: أفضل إعداد لببجي أو يوتيوب 4K)..."
                            } else {
                                "Ask AI for custom tuning (e.g. best setup for gaming or 4K)..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JissrCyan,
                        unfocusedBorderColor = BorderSubtleDark,
                        focusedContainerColor = ObsidianSurfaceVariant.copy(alpha = 0.65f),
                        unfocusedContainerColor = ObsidianSurfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_custom_prompt_input")
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (customPrompt.isNotBlank() && !isBusy) JissrCyan else ObsidianCardElevated,
                    border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (customPrompt.isNotBlank()) {
                                onStreamCustomQuestion(customPrompt)
                            }
                        },
                        enabled = customPrompt.isNotBlank() && !isBusy,
                        modifier = Modifier.testTag("ai_send_custom_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (isArabic) "إرسال للذكاء الاصطناعي" else "Ask AI",
                            tint = if (customPrompt.isNotBlank() && !isBusy) Color(0xFF032235) else JissrCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Dynamic AI Output State Rendering
            when (aiState) {
                is AiOptimizationUiState.Idle -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianSurfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, BorderSubtleDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isArabic) {
                                    "اضغط على زر التحليل ليقوم الذكاء الاصطناعي بفحص سرعة الإنترنت الحالية وزمن الاستجابة واختيار أفضل خادم وبروتوكول وMTU تلقائياً."
                                } else {
                                    "Tap Analyze to let Gemini AI inspect your live speed, latency, and relay nodes to recommend the optimal server, protocol, DNS, and MTU."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is AiOptimizationUiState.Loading -> {
                    // Handled by button spinner + subtle status banner
                }

                is AiOptimizationUiState.Streaming -> {
                    if (aiState.plan != null) {
                        AiPlanResultContent(
                            isArabic = isArabic,
                            plan = aiState.plan,
                            applied = false,
                            onApplyPlan = { onApplyAiPlan(aiState.plan) }
                        )
                    }
                    if (aiState.partialText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianCardElevated,
                            border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_streaming_response_box")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        strokeWidth = 1.8.dp,
                                        color = JissrCyan
                                    )
                                    Text(
                                        text = if (isArabic) "توصيات الذكاء الاصطناعي المباشرة:" else "Live AI Advisory Stream:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JissrCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = aiState.partialText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is AiOptimizationUiState.Success -> {
                    AiPlanResultContent(
                        isArabic = isArabic,
                        plan = aiState.plan,
                        applied = aiState.applied,
                        onApplyPlan = { onApplyAiPlan(aiState.plan) }
                    )

                    AnimatedVisibility(visible = aiState.advisoryText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianCardElevated,
                            border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_advisory_result_box")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "استشارة الذكاء الاصطناعي المخصصة:" else "Custom AI Network Advisory:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JissrCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = aiState.advisoryText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is AiOptimizationUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = JissrCrimson.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, JissrCrimson.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_optimization_error_box")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = JissrCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = aiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedButton(
                                onClick = onRunAiOptimization,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, JissrCrimson),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = JissrCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "إعادة المحاولة" else "Retry",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JissrCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiPlanResultContent(
    isArabic: Boolean,
    plan: AiTunnelOptimizationPlan,
    applied: Boolean,
    onApplyPlan: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurfaceVariant,
        border = BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_optimization_plan_result")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.headline,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = JissrEmerald.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, JissrEmerald)
                ) {
                    Text(
                        text = "+${plan.estimatedSpeedGainPercent}% BOOST",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = JissrEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // 4 Recommended Technical Parameters Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiParameterBadge(
                    icon = Icons.Filled.Public,
                    label = if (isArabic) "الخادم الأمثل" else "Best Server",
                    value = plan.recommendedServerCity,
                    accent = JissrEmerald,
                    modifier = Modifier.weight(1f)
                )
                AiParameterBadge(
                    icon = Icons.Filled.Security,
                    label = if (isArabic) "البروتوكول" else "Protocol",
                    value = plan.recommendedProtocol,
                    accent = JissrCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiParameterBadge(
                    icon = Icons.Filled.Tune,
                    label = if (isArabic) "حجم MTU الذكي" else "Optimal MTU",
                    value = "${plan.recommendedMtu}",
                    accent = JissrAmber,
                    modifier = Modifier.weight(1f)
                )
                AiParameterBadge(
                    icon = Icons.Filled.Dns,
                    label = if (isArabic) "أسرع DNS" else "Smart DNS",
                    value = plan.recommendedDns,
                    accent = JissrEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            // Recommended Steps List
            if (plan.optimizationSteps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.optimizationSteps.forEachIndexed { idx, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = JissrCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 10.sp
                                        ),
                                        color = JissrCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onApplyPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("apply_ai_optimization_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (applied) ObsidianCardElevated else JissrCyan,
                    contentColor = if (applied) JissrEmerald else Color(0xFF032235)
                ),
                border = if (applied) BorderStroke(1.2.dp, JissrEmerald) else null
            ) {
                Icon(
                    imageVector = if (applied) Icons.Filled.CheckCircle else Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (applied) {
                        if (isArabic) "تم تطبيق تحسينات الذكاء الاصطناعي بنجاح ✓" else "AI Optimizations Applied to Tunnel ✓"
                    } else {
                        if (isArabic) "تطبيق الإعدادات الذكية على النفق فوراً" else "Apply AI Settings to Tunnel Now"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AiParameterBadge(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianCardElevated,
        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
