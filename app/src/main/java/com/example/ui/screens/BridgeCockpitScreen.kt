package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.data.local.BridgeNodeEntity
import com.example.network.SpeedTestProgress
import com.example.network.SpeedTestStage
import com.example.ui.JissrViewModel
import com.example.ui.components.JissrAiOptimizerCard
import com.example.ui.components.Tunnel24HourTrendCard
import com.example.ui.i18n.JissrStrings
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrAmber
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.vpn.SmartBridgeMode
import com.example.vpn.TunnelConnectionStatus
import com.example.vpn.VpnTunnelState
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BridgeCockpitScreen(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    speedTestProgress: SpeedTestProgress,
    aiState: AiOptimizationUiState,
    selectedAiGoal: AiOptimizationGoal,
    onSelectAiGoal: (AiOptimizationGoal) -> Unit,
    onRunAiOptimization: () -> Unit,
    onApplyAiPlan: (AiTunnelOptimizationPlan) -> Unit,
    onStreamCustomAiQuestion: (String) -> Unit,
    onToggleConnection: () -> Unit,
    onSmartFastestConnect: () -> Unit,
    onRunSpeedTest: () -> Unit,
    onSelectSmartMode: (SmartBridgeMode) -> Unit,
    onOpenRelayPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = tunnelState.status == TunnelConnectionStatus.CONNECTED
    val isConnecting = tunnelState.status == TunnelConnectionStatus.CONNECTING

    val accentColor by animateColorAsState(
        targetValue = when (tunnelState.status) {
            TunnelConnectionStatus.CONNECTED -> JissrEmerald
            TunnelConnectionStatus.CONNECTING -> JissrAmber
            TunnelConnectionStatus.DISCONNECTED -> JissrCyan
        },
        animationSpec = tween(450),
        label = "accentColor"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bridge_cockpit_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SystemArchitectureModulesStrip(
                strings = strings,
                tunnelState = tunnelState,
                isConnected = isConnected
            )
        }

        item {
            BridgeHeroVisualizerCard(
                strings = strings,
                tunnelState = tunnelState,
                activeNode = activeNode,
                accentColor = accentColor,
                isConnected = isConnected,
                isConnecting = isConnecting,
                onToggleConnection = onToggleConnection,
                onSmartFastestConnect = onSmartFastestConnect
            )
        }

        // AI Neural Tunnel & Speed Optimizer Card (Gemini 3.8 Flash)
        item {
            JissrAiOptimizerCard(
                isArabic = strings.isArabic,
                aiState = aiState,
                selectedGoal = selectedAiGoal,
                onSelectGoal = onSelectAiGoal,
                onRunAiOptimization = onRunAiOptimization,
                onApplyAiPlan = onApplyAiPlan,
                onStreamCustomQuestion = onStreamCustomAiQuestion
            )
        }

        // Prominent Interactive Internet Speedometer & Speed Test Card
        item {
            InternetSpeedometerCard(
                strings = strings,
                speedProgress = speedTestProgress,
                liveDownloadBps = tunnelState.downloadSpeedBps,
                liveUploadBps = tunnelState.uploadSpeedBps,
                onRunSpeedTest = onRunSpeedTest
            )
        }

        // Recharts-Style 24-Hour Tunnel Latency & Average Speed Trend Graph
        item {
            Tunnel24HourTrendCard(
                strings = strings,
                tunnelState = tunnelState,
                activeNode = activeNode,
                speedTestProgress = speedTestProgress
            )
        }

        item {
            ActiveRelaySelectorCard(
                strings = strings,
                tunnelState = tunnelState,
                activeNode = activeNode,
                onClick = onOpenRelayPicker
            )
        }

        item {
            SmartBridgeModesGrid(
                strings = strings,
                selectedMode = tunnelState.smartMode,
                onSelectMode = onSelectSmartMode
            )
        }

        item {
            LiveThroughputTelemetryCard(
                strings = strings,
                tunnelState = tunnelState,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun SystemArchitectureModulesStrip(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("system_architecture_strip"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SystemModuleBadge(
            iconRes = R.drawable.ic_jissr_logo_crest,
            title = if (strings.isArabic) "نواة الجسر" else "Bridge Core",
            value = tunnelState.activeProtocol,
            accent = JissrEmerald,
            modifier = Modifier.weight(1f)
        )
        SystemModuleBadge(
            iconRes = R.drawable.ic_quantum_shield_badge,
            title = if (strings.isArabic) "درع التشفير" else "Quantum Shield",
            value = if (tunnelState.killSwitchEnabled) "AES-GCM" else "ChaCha20",
            accent = JissrCyan,
            modifier = Modifier.weight(1f)
        )
        SystemModuleBadge(
            iconRes = R.drawable.ic_turbo_tunnel_bolt,
            title = if (strings.isArabic) "تسريع المسار" else "Turbo Route",
            value = if (isConnected) "ACTIVE" else "READY",
            accent = if (isConnected) JissrEmerald else JissrAmber,
            modifier = Modifier.weight(1f)
        )
        SystemModuleBadge(
            iconRes = R.drawable.ic_cyber_radar_orb,
            title = if (strings.isArabic) "رادار DNS" else "DNS Radar",
            value = tunnelState.dnsServer,
            accent = JissrCyan,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SystemModuleBadge(
    @DrawableRes iconRes: Int,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.48f), BorderSubtleDark)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                ),
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InternetSpeedometerCard(
    strings: JissrStrings,
    speedProgress: SpeedTestProgress,
    liveDownloadBps: Long,
    liveUploadBps: Long,
    onRunSpeedTest: () -> Unit
) {
    val animatedMbps by animateFloatAsState(
        targetValue = speedProgress.currentGaugeMbps.coerceIn(0f, 250f),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 180f),
        label = "animatedMbps"
    )

    val stageLabel = when (speedProgress.stage) {
        SpeedTestStage.IDLE -> strings.speedStageIdle
        SpeedTestStage.PING -> strings.speedStagePing
        SpeedTestStage.DOWNLOAD -> strings.speedStageDownload
        SpeedTestStage.UPLOAD -> strings.speedStageUpload
        SpeedTestStage.COMPLETED -> strings.speedStageDone
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("internet_speedometer_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(JissrEmerald.copy(alpha = 0.55f), JissrCyan.copy(alpha = 0.45f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JissrCyan.copy(alpha = 0.08f),
                            ObsidianSurface,
                            ObsidianSurfaceVariant.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        shape = RoundedCornerShape(12.dp),
                        color = JissrEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cyber_radar_orb),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = strings.speedMeterTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.speedMeterSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Instantaneous Speed Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianCardElevated,
                    border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "↓ ${JissrViewModel.formatBytes(liveDownloadBps)}/s",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = JissrCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Circular Analog/Digital Cyber Speedometer Gauge with 24 Precision Radial Ticks
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(210.dp, 175.dp)
                ) {
                    val strokeWidth = 14.dp.toPx()
                    val arcDiameter = size.width.coerceAtMost(size.height) - strokeWidth - 16.dp.toPx()
                    val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f + 12f)
                    val arcSize = Size(arcDiameter, arcDiameter)
                    val center = Offset(size.width / 2f, topLeft.y + arcDiameter / 2f)

                    // Subtle inner ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(JissrEmerald.copy(alpha = 0.16f), Color.Transparent),
                            center = center,
                            radius = arcDiameter * 0.55f
                        ),
                        center = center,
                        radius = arcDiameter * 0.55f
                    )

                    // Background track arc (150 deg to 390 deg => 240 deg sweep)
                    drawArc(
                        color = ObsidianCardElevated,
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val sweepFraction = (animatedMbps / 200f).coerceIn(0.02f, 1f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(JissrCyan, JissrEmerald, JissrAmber, JissrEmerald)
                        ),
                        startAngle = 150f,
                        sweepAngle = 240f * sweepFraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 24 Outer Radial Precision Ticks
                    val tickCount = 24
                    val outerTickRadius = (arcDiameter / 2f) + 12.dp.toPx()
                    val innerTickRadius = (arcDiameter / 2f) + 7.dp.toPx()
                    for (i in 0..tickCount) {
                        val tickFraction = i.toFloat() / tickCount
                        val tickAngleRad = Math.toRadians(150.0 + 240.0 * tickFraction)
                        val isLit = tickFraction <= sweepFraction
                        val startPt = Offset(
                            x = center.x + (cos(tickAngleRad) * innerTickRadius).toFloat(),
                            y = center.y + (sin(tickAngleRad) * innerTickRadius).toFloat()
                        )
                        val endPt = Offset(
                            x = center.x + (cos(tickAngleRad) * outerTickRadius).toFloat(),
                            y = center.y + (sin(tickAngleRad) * outerTickRadius).toFloat()
                        )
                        drawLine(
                            color = if (isLit) JissrEmerald else BorderSubtleDark,
                            start = startPt,
                            end = endPt,
                            strokeWidth = if (i % 4 == 0) 2.4.dp.toPx() else 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Speedometer Needle
                    val angleDeg = 150.0 + (240.0 * sweepFraction)
                    val angleRad = Math.toRadians(angleDeg)
                    val needleLen = (arcDiameter / 2f) - 18.dp.toPx()
                    val needleEnd = Offset(
                        x = center.x + (cos(angleRad) * needleLen).toFloat(),
                        y = center.y + (sin(angleRad) * needleLen).toFloat()
                    )
                    drawLine(
                        color = JissrEmerald,
                        start = center,
                        end = needleEnd,
                        strokeWidth = 3.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = JissrEmerald,
                        radius = 6.5.dp.toPx(),
                        center = center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 30.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", animatedMbps),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 34.sp
                        ),
                        color = JissrEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4-Metric Speed Summary Row (Download Mbps, Upload Mbps, Ping ms, Jitter ms)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedStatPill(
                    title = strings.downloadSpeedMbps,
                    value = String.format(Locale.US, "%.1f Mbps", speedProgress.downloadMbps),
                    accent = JissrEmerald,
                    modifier = Modifier.weight(1.1f)
                )
                SpeedStatPill(
                    title = strings.uploadSpeedMbps,
                    value = String.format(Locale.US, "%.1f Mbps", speedProgress.uploadMbps),
                    accent = JissrCyan,
                    modifier = Modifier.weight(1.1f)
                )
                SpeedStatPill(
                    title = strings.pingMsLabel,
                    value = "${speedProgress.pingMs} ms",
                    accent = JissrEmerald,
                    modifier = Modifier.weight(0.85f)
                )
                SpeedStatPill(
                    title = strings.jitterMsLabel,
                    value = "${speedProgress.jitterMs} ms",
                    accent = JissrAmber,
                    modifier = Modifier.weight(0.85f)
                )
            }

            if (speedProgress.isRunning) {
                LinearProgressIndicator(
                    progress = { speedProgress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = JissrEmerald,
                    trackColor = ObsidianSurfaceVariant
                )
            }

            Text(
                text = if (!speedProgress.isRunning && speedProgress.downloadMbps >= 40f) {
                    strings.speedQualityExcellent
                } else if (!speedProgress.isRunning && speedProgress.downloadMbps > 0f) {
                    strings.speedQualityGood
                } else {
                    stageLabel
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (speedProgress.isRunning) JissrCyan else JissrEmerald
            )

            Button(
                onClick = onRunSpeedTest,
                enabled = !speedProgress.isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_speed_test_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrCyan,
                    contentColor = Color(0xFF042233)
                )
            ) {
                if (speedProgress.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF042233)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.testingSpeedBtn, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.startSpeedTestBtn, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SpeedStatPill(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceVariant,
        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
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

@Composable
private fun BridgeHeroVisualizerCard(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    accentColor: Color,
    isConnected: Boolean,
    isConnecting: Boolean,
    onToggleConnection: () -> Unit,
    onSmartFastestConnect: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bridgePulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )
    val glowBreath by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBreath"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.7f),
                    JissrCyan.copy(alpha = 0.45f),
                    accentColor.copy(alpha = 0.55f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.18f),
                            ObsidianSurface,
                            ObsidianSurfaceVariant.copy(alpha = 0.78f)
                        )
                    )
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
            ) {
                val w = size.width
                val h = size.height
                val deckY = h * 0.74f
                val leftTowerX = w * 0.20f
                val rightTowerX = w * 0.80f
                val towerTopY = h * 0.20f

                // Perspective Horizon Cyber Grid
                for (row in 1..4) {
                    val gridY = deckY + (row * 14f)
                    if (gridY < h) {
                        drawLine(
                            color = JissrCyan.copy(alpha = 0.08f),
                            start = Offset(0f, gridY),
                            end = Offset(w, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isConnected) 0.36f * glowBreath else 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.46f),
                        radius = w * 0.48f
                    ),
                    center = Offset(w * 0.5f, h * 0.46f),
                    radius = w * 0.48f
                )

                val cablePath = Path().apply {
                    moveTo(0f, deckY - 12f)
                    quadraticBezierTo(leftTowerX * 0.5f, deckY - 8f, leftTowerX, towerTopY)
                    quadraticBezierTo(w * 0.5f, deckY - 18f, rightTowerX, towerTopY)
                    quadraticBezierTo(w - (leftTowerX * 0.5f), deckY - 8f, w, deckY - 12f)
                }
                drawPath(
                    path = cablePath,
                    color = accentColor.copy(alpha = if (isConnected) 0.62f else 0.28f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val suspenderCount = 14
                for (i in 1 until suspenderCount) {
                    val x = (w / suspenderCount) * i
                    val normalized = (x - w * 0.5f) / (w * 0.5f)
                    val archHeight = towerTopY + (1f - (normalized * normalized).coerceIn(0f, 1f)) * (deckY - towerTopY - 22f)
                    drawLine(
                        color = accentColor.copy(alpha = 0.20f),
                        start = Offset(x, archHeight),
                        end = Offset(x, deckY),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }

                drawLine(
                    color = accentColor.copy(alpha = 0.52f),
                    start = Offset(leftTowerX, towerTopY - 8f),
                    end = Offset(leftTowerX, deckY + 18f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.52f),
                    start = Offset(rightTowerX, towerTopY - 8f),
                    end = Offset(rightTowerX, deckY + 18f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = accentColor.copy(alpha = 0.72f),
                    start = Offset(0f, deckY),
                    end = Offset(w, deckY),
                    strokeWidth = 2.5.dp.toPx()
                )

                if (isConnected || isConnecting) {
                    for (packetIdx in 0..4) {
                        val progress = (pulsePhase + packetIdx * 0.2f) % 1f
                        val packetX = progress * w
                        val waveOffset = sin(progress * Math.PI * 2).toFloat() * 4f
                        drawCircle(
                            color = accentColor,
                            radius = 4.dp.toPx(),
                            center = Offset(packetX, deckY + waveOffset)
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = Offset(packetX, deckY + waveOffset)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accentColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                text = when (tunnelState.status) {
                                    TunnelConnectionStatus.CONNECTED -> strings.statusConnected
                                    TunnelConnectionStatus.CONNECTING -> strings.statusConnecting
                                    TunnelConnectionStatus.DISCONNECTED -> strings.statusDisconnected
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = ObsidianCardElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.42f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_quantum_shield_badge),
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${tunnelState.securityScorePercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = JissrEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3-Ring Rotating Orbital Power Core
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(134.dp)
                        .clip(CircleShape)
                        .clickable { onToggleConnection() }
                        .testTag("connect_vpn_overlay_orb")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.35f * glowBreath),
                                    Color.Transparent
                                )
                            ),
                            radius = radius
                        )
                        rotate(degrees = orbitAngle) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        accentColor.copy(alpha = 0.85f),
                                        JissrCyan.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                ),
                                startAngle = 0f,
                                sweepAngle = 250f,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(102.dp),
                        shape = CircleShape,
                        color = ObsidianSurfaceVariant,
                        border = BorderStroke(
                            2.5.dp,
                            Brush.linearGradient(
                                colors = listOf(accentColor, JissrCyan)
                            )
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isConnected) {
                                        R.drawable.ic_quantum_shield_badge
                                    } else {
                                        R.drawable.ic_jissr_logo_crest
                                    }
                                ),
                                contentDescription = strings.btnEngage,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isConnected) "PROTECTED" else if (isConnecting) "SYNCING" else "TAP TO START",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp
                                ),
                                color = accentColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = JissrViewModel.formatDuration(tunnelState.uptimeSeconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TUN ${tunnelState.virtualIp}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dns,
                            contentDescription = null,
                            tint = JissrCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "DNS ${tunnelState.dnsServer}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (activeNode != null && activeNode.lastLatencyMs > 0) {
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${activeNode.lastLatencyMs} ms",
                            style = MaterialTheme.typography.labelMedium,
                            color = JissrEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onToggleConnection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("connect_vpn_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) ObsidianCardElevated else JissrEmerald,
                        contentColor = if (isConnected) JissrEmerald else Color(0xFF032017)
                    ),
                    border = if (isConnected) BorderStroke(1.5.dp, JissrEmerald) else null
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Filled.CheckCircle else Icons.Filled.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) strings.btnDisconnect else strings.btnEngage,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSmartFastestConnect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("smart_fastest_node_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.smartFastestRelay,
                        style = MaterialTheme.typography.labelLarge,
                        color = JissrCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveRelaySelectorCard(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("active_relay_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCardElevated,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tunnelState.activeFlag,
                            fontSize = 26.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = "${tunnelState.displayCity(strings.isArabic)} · ${tunnelState.displayCountry(strings.isArabic)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${tunnelState.activeProtocol} · ${tunnelState.activeObfuscation} · ${
                            if (activeNode != null && activeNode.lastLatencyMs > 0) "${activeNode.lastLatencyMs} ms" else "MTU ${tunnelState.mtu}"
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JissrCyan
                    )
                    Text(
                        text = strings.activeRelayChangeHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = ObsidianCardElevated,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = strings.navRelays,
                        tint = JissrEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartBridgeModesGrid(
    strings: JissrStrings,
    selectedMode: SmartBridgeMode,
    onSelectMode: (SmartBridgeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = strings.modeSectionTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartModeCard(
                title = strings.modeStealth,
                subtitle = strings.modeStealthDesc,
                icon = Icons.Filled.VisibilityOff,
                accent = JissrEmerald,
                isSelected = selectedMode == SmartBridgeMode.STEALTH,
                tag = "mode_stealth_card",
                onClick = { onSelectMode(SmartBridgeMode.STEALTH) },
                modifier = Modifier.weight(1f)
            )
            SmartModeCard(
                title = strings.modeGaming,
                subtitle = strings.modeGamingDesc,
                icon = Icons.Filled.SportsEsports,
                accent = JissrCyan,
                isSelected = selectedMode == SmartBridgeMode.GAMING,
                tag = "mode_gaming_card",
                onClick = { onSelectMode(SmartBridgeMode.GAMING) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartModeCard(
                title = strings.modeStreaming,
                subtitle = strings.modeStreamingDesc,
                icon = Icons.Filled.MovieFilter,
                accent = JissrAmber,
                isSelected = selectedMode == SmartBridgeMode.STREAMING,
                tag = "mode_streaming_card",
                onClick = { onSelectMode(SmartBridgeMode.STREAMING) },
                modifier = Modifier.weight(1f)
            )
            SmartModeCard(
                title = strings.modeDoubleHop,
                subtitle = strings.modeDoubleHopDesc,
                icon = Icons.Filled.Hub,
                accent = JissrEmerald,
                isSelected = selectedMode == SmartBridgeMode.DOUBLE_HOP,
                tag = "mode_double_hop_card",
                onClick = { onSelectMode(SmartBridgeMode.DOUBLE_HOP) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmartModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ObsidianCardElevated else ObsidianSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accent else BorderSubtleDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.16f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveThroughputTelemetryCard(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.throughputTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${tunnelState.tunPacketsInspected} ${strings.packetsInspected}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JissrEmerald
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = strings.downloadLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = JissrEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${JissrViewModel.formatBytes(tunnelState.downloadSpeedBps)}/s",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${strings.totalLabel}: ${JissrViewModel.formatBytes(tunnelState.totalSessionRxBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = strings.uploadLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = JissrCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${JissrViewModel.formatBytes(tunnelState.uploadSpeedBps)}/s",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${strings.totalLabel}: ${JissrViewModel.formatBytes(tunnelState.totalSessionTxBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val samples = tunnelState.throughputHistoryKbps
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                val w = size.width
                val h = size.height
                val maxVal = (samples.maxOrNull() ?: 10f).coerceAtLeast(10f)
                val stepX = if (samples.size > 1) w / (samples.size - 1) else w

                val linePath = Path()
                val fillPath = Path()

                samples.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normalizedY = (value / maxVal).coerceIn(0.05f, 1f)
                    val y = h - (normalizedY * (h - 8f)) - 4f
                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(w, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
                drawPath(
                    path = linePath,
                    color = accentColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
