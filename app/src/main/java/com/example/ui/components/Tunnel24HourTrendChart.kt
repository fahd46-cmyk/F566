package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BridgeNodeEntity
import com.example.network.SpeedTestProgress
import com.example.ui.i18n.JissrStrings
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrAmber
import com.example.ui.theme.JissrCrimson
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.vpn.SmartBridgeMode
import com.example.vpn.VpnTunnelState
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

enum class TrendChartViewMode {
    DUAL_OVERLAY,
    LATENCY_PEAKS,
    SPEED_OVER_TIME
}

enum class TrendWindowHours(val hours: Int, val labelEn: String, val labelAr: String) {
    HOURS_24(24, "24H", "24س"),
    HOURS_12(12, "12H", "12س"),
    HOURS_6(6, "6H", "6س")
}

data class HourlyTunnelPoint(
    val index: Int,
    val timeLabel: String,
    val avgLatencyMs: Int,
    val peakLatencyMs: Int,
    val avgSpeedMbps: Float,
    val peakSpeedMbps: Float,
    val isMajorPeakHour: Boolean = false
)

@Composable
fun Tunnel24HourTrendCard(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    speedTestProgress: SpeedTestProgress,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(TrendChartViewMode.DUAL_OVERLAY) }
    var selectedWindow by remember { mutableStateOf(TrendWindowHours.HOURS_24) }

    val full24hSeries = remember(
        tunnelState.activeNodeId,
        tunnelState.smartMode,
        activeNode?.lastLatencyMs,
        speedTestProgress.downloadMbps,
        speedTestProgress.pingMs,
        strings.isArabic
    ) {
        build24HourTunnelSeries(
            tunnelState = tunnelState,
            activeNode = activeNode,
            speedTestProgress = speedTestProgress,
            isArabic = strings.isArabic
        )
    }

    val visiblePoints = remember(full24hSeries, selectedWindow) {
        full24hSeries.takeLast(selectedWindow.hours)
    }

    var selectedPointIndex by remember(visiblePoints.size) {
        mutableIntStateOf(visiblePoints.lastIndex.coerceAtLeast(0))
    }

    val safeIndex = selectedPointIndex.coerceIn(0, (visiblePoints.size - 1).coerceAtLeast(0))
    val inspectedPoint = visiblePoints.getOrNull(safeIndex)

    val overallAvgLatency = remember(visiblePoints) {
        if (visiblePoints.isEmpty()) 0 else visiblePoints.map { it.avgLatencyMs }.average().roundToInt()
    }
    val maxPeakPoint = remember(visiblePoints) {
        visiblePoints.maxByOrNull { it.peakLatencyMs }
    }
    val overallAvgSpeed = remember(visiblePoints) {
        if (visiblePoints.isEmpty()) 0f else visiblePoints.map { it.avgSpeedMbps }.average().toFloat()
    }
    val maxPeakSpeed = remember(visiblePoints) {
        visiblePoints.maxOfOrNull { it.peakSpeedMbps } ?: 0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tunnel_24h_trend_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.2.dp, JissrCyan.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header + Time Window Selector (24H / 12H / 6H)
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
                        color = JissrCyan.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.ShowChart,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = strings.trend24hTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${tunnelState.activeFlag} ${tunnelState.displayCity(strings.isArabic)} · ${strings.trend24hSubtitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 24H / 12H / 6H Window Switcher
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrendWindowHours.entries.forEach { window ->
                        val isSelected = selectedWindow == window
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) JissrCyan.copy(alpha = 0.22f) else ObsidianSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) JissrCyan else BorderSubtleDark
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedWindow = window
                                    selectedPointIndex = window.hours - 1
                                }
                                .testTag("trend_range_${window.hours}h")
                        ) {
                            Text(
                                text = if (strings.isArabic) window.labelAr else window.labelEn,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = if (isSelected) JissrCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Recharts Series Mode Selector (Dual Overlay / Latency & Peaks / Speed Mbps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(TrendChartViewMode.DUAL_OVERLAY, strings.trendModeDual, "trend_mode_dual"),
                    Triple(TrendChartViewMode.LATENCY_PEAKS, strings.trendModeLatency, "trend_mode_latency"),
                    Triple(TrendChartViewMode.SPEED_OVER_TIME, strings.trendModeSpeed, "trend_mode_speed")
                )

                modes.forEach { (mode, label, tag) ->
                    val selected = selectedMode == mode
                    val activeAccent = when (mode) {
                        TrendChartViewMode.DUAL_OVERLAY -> JissrCyan
                        TrendChartViewMode.LATENCY_PEAKS -> JissrAmber
                        TrendChartViewMode.SPEED_OVER_TIME -> JissrEmerald
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) activeAccent.copy(alpha = 0.18f) else ObsidianSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (selected) activeAccent else BorderSubtleDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedMode = mode }
                            .testTag(tag)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 4-Metric KPI Summary Strip (24h Avg Ping, Peak Ping, Avg Speed, Peak Speed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrendSummaryKpiTile(
                    label = strings.trendAvgLatencyLabel,
                    value = "$overallAvgLatency ms",
                    accentColor = JissrCyan,
                    modifier = Modifier.weight(1f)
                )
                TrendSummaryKpiTile(
                    label = strings.trendPeakLatencyLabel,
                    value = "${maxPeakPoint?.peakLatencyMs ?: 0} ms",
                    subValue = maxPeakPoint?.timeLabel,
                    accentColor = JissrAmber,
                    modifier = Modifier.weight(1f)
                )
                TrendSummaryKpiTile(
                    label = strings.trendAvgSpeedLabel,
                    value = String.format(Locale.US, "%.1f Mbps", overallAvgSpeed),
                    accentColor = JissrEmerald,
                    modifier = Modifier.weight(1f)
                )
                TrendSummaryKpiTile(
                    label = strings.trendPeakSpeedLabel,
                    value = String.format(Locale.US, "%.1f Mbps", maxPeakSpeed),
                    accentColor = JissrEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            // Recharts-Style Active Point Tooltip Header
            if (inspectedPoint != null) {
                RechartsActiveTooltipBanner(
                    strings = strings,
                    point = inspectedPoint,
                    activeFlag = tunnelState.activeFlag,
                    activeCity = tunnelState.displayCity(strings.isArabic),
                    isMajorPeak = inspectedPoint.peakLatencyMs == maxPeakPoint?.peakLatencyMs
                )
            }

            // Interactive Recharts Composed Area + Line + Peak Spike Canvas
            RechartsComposedTrendCanvas(
                points = visiblePoints,
                selectedIndex = safeIndex,
                viewMode = selectedMode,
                avgLatencyMs = overallAvgLatency,
                avgSpeedMbps = overallAvgSpeed,
                avgLabel = strings.trendAvgLineLabel,
                peakLabel = strings.trendPeakBadge,
                onSelectIndex = { idx ->
                    selectedPointIndex = idx.coerceIn(0, (visiblePoints.size - 1).coerceAtLeast(0))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
                    .testTag("recharts_24h_latency_canvas")
            )

            // Recharts Legend & Touch Scrub Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDotItem(
                        color = JissrCyan,
                        label = "${strings.pingMsLabel} (ms)"
                    )
                    LegendDotItem(
                        color = JissrAmber,
                        label = strings.trendPeakLatencyLabel
                    )
                    LegendDotItem(
                        color = JissrEmerald,
                        label = "${strings.trendAvgSpeedLabel} (Mbps)"
                    )
                }

                Text(
                    text = "${selectedWindow.hours}h · ${tunnelState.activeProtocol}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = strings.trendTouchHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TrendSummaryKpiTile(
    label: String,
    value: String,
    accentColor: Color,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceVariant,
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (!subValue.isNullOrBlank()) {
                Text(
                    text = "@ $subValue",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RechartsActiveTooltipBanner(
    strings: JissrStrings,
    point: HourlyTunnelPoint,
    activeFlag: String,
    activeCity: String,
    isMajorPeak: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ObsidianCardElevated,
        border = BorderStroke(
            width = 1.dp,
            color = if (isMajorPeak) JissrAmber.copy(alpha = 0.8f) else JissrCyan.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_active_tooltip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = JissrCyan.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = point.timeLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = JissrCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "$activeFlag $activeCity",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isMajorPeak) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JissrAmber.copy(alpha = 0.22f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WarningAmber,
                                        contentDescription = null,
                                        tint = JissrAmber,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = strings.trendPeakBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JissrAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "${strings.trendAvgLatencyLabel}: ${point.avgLatencyMs}ms · ${strings.trendPeakLatencyLabel}: ${point.peakLatencyMs}ms",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = JissrAmber
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f Mbps", point.avgSpeedMbps),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = JissrEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Peak ${String.format(Locale.US, "%.1f", point.peakSpeedMbps)} Mbps",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RechartsComposedTrendCanvas(
    points: List<HourlyTunnelPoint>,
    selectedIndex: Int,
    viewMode: TrendChartViewMode,
    avgLatencyMs: Int,
    avgSpeedMbps: Float,
    avgLabel: String,
    peakLabel: String,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxLatency = remember(points) {
        (points.maxOfOrNull { it.peakLatencyMs } ?: 100).coerceAtLeast(60) * 1.15f
    }
    val minLatency = remember(points) {
        ((points.minOfOrNull { it.avgLatencyMs } ?: 10) * 0.7f).coerceAtLeast(5f)
    }

    val maxSpeed = remember(points) {
        (points.maxOfOrNull { it.peakSpeedMbps } ?: 220f).coerceAtLeast(80f) * 1.12f
    }
    val minSpeed = remember(points) {
        ((points.minOfOrNull { it.avgSpeedMbps } ?: 30f) * 0.75f).coerceAtLeast(10f)
    }

    val highestPeakIndex = remember(points) {
        points.indices.maxByOrNull { points[it].peakLatencyMs } ?: 0
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF070E1A),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points.size) {
                    detectTapGestures { offset ->
                        val leftPad = 36.dp.toPx()
                        val rightPad = 36.dp.toPx()
                        val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                        val fraction = ((offset.x - leftPad) / plotWidth).coerceIn(0f, 1f)
                        val idx = (fraction * (points.size - 1)).roundToInt()
                        onSelectIndex(idx)
                    }
                }
                .pointerInput(points.size) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val leftPad = 36.dp.toPx()
                        val rightPad = 36.dp.toPx()
                        val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                        val fraction = ((change.position.x - leftPad) / plotWidth).coerceIn(0f, 1f)
                        val idx = (fraction * (points.size - 1)).roundToInt()
                        onSelectIndex(idx)
                    }
                }
        ) {
            val leftPad = 38.dp.toPx()
            val rightPad = 40.dp.toPx()
            val topPad = 24.dp.toPx()
            val bottomPad = 28.dp.toPx()

            val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(10f)
            val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(10f)
            val stepX = if (points.size > 1) plotWidth / (points.size - 1) else plotWidth

            val axisTextPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5.dp.toPx()
                typeface = Typeface.MONOSPACE
                color = android.graphics.Color.argb(175, 148, 163, 184)
            }

            val rightAxisPaint = Paint(axisTextPaint).apply {
                color = android.graphics.Color.argb(190, 0, 229, 163)
                textAlign = Paint.Align.RIGHT
            }

            // 1. Draw Recharts CartesianGrid (Horizontal & Vertical Dashed Lines + Axis Labels)
            val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val ratio = i.toFloat() / gridSteps
                val y = topPad + plotHeight * ratio
                drawLine(
                    color = Color(0xFF1E2D45).copy(alpha = 0.65f),
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + plotWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = gridDash
                )

                // Left Y-Axis Labels (Latency ms or Speed if SPEED_OVER_TIME)
                val leftValueText = if (viewMode == TrendChartViewMode.SPEED_OVER_TIME) {
                    val spd = maxSpeed - ratio * (maxSpeed - minSpeed)
                    "${spd.roundToInt()}M"
                } else {
                    val lat = maxLatency - ratio * (maxLatency - minLatency)
                    "${lat.roundToInt()}ms"
                }
                drawContext.canvas.nativeCanvas.drawText(
                    leftValueText,
                    4.dp.toPx(),
                    y + 3.dp.toPx(),
                    axisTextPaint
                )

                // Right Y-Axis Labels (Speed Mbps in Dual Overlay or Peak Latency)
                val rightValueText = if (viewMode == TrendChartViewMode.LATENCY_PEAKS) {
                    val lat = maxLatency - ratio * (maxLatency - minLatency)
                    "${lat.roundToInt()}ms"
                } else {
                    val spd = maxSpeed - ratio * (maxSpeed - minSpeed)
                    "${spd.roundToInt()}M"
                }
                drawContext.canvas.nativeCanvas.drawText(
                    rightValueText,
                    size.width - 4.dp.toPx(),
                    y + 3.dp.toPx(),
                    rightAxisPaint
                )
            }

            // Vertical X-Axis Grid & Time Labels
            val labelStride = max(1, points.size / 6)
            points.forEachIndexed { idx, pt ->
                if (idx % labelStride == 0 || idx == points.lastIndex) {
                    val x = leftPad + idx * stepX
                    drawLine(
                        color = Color(0xFF1E2D45).copy(alpha = 0.4f),
                        start = Offset(x, topPad),
                        end = Offset(x, topPad + plotHeight),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = gridDash
                    )

                    val xPaint = Paint(axisTextPaint).apply {
                        textAlign = Paint.Align.CENTER
                        if (idx == selectedIndex) {
                            color = android.graphics.Color.argb(255, 0, 210, 255)
                            isFakeBoldText = true
                        }
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        pt.timeLabel,
                        x,
                        size.height - 7.dp.toPx(),
                        xPaint
                    )
                }
            }

            // Coordinate mappers
            fun latencyToY(ms: Float): Float {
                val norm = ((ms - minLatency) / (maxLatency - minLatency).coerceAtLeast(1f)).coerceIn(0f, 1f)
                return topPad + plotHeight * (1f - norm)
            }

            fun speedToY(mbps: Float): Float {
                val norm = ((mbps - minSpeed) / (maxSpeed - minSpeed).coerceAtLeast(1f)).coerceIn(0f, 1f)
                return topPad + plotHeight * (1f - norm)
            }

            // 2. Draw Average Latency Area + Smooth Monotone Curve (when DUAL or LATENCY mode)
            if (viewMode == TrendChartViewMode.DUAL_OVERLAY || viewMode == TrendChartViewMode.LATENCY_PEAKS) {
                val avgLatOffsets = points.mapIndexed { idx, pt ->
                    Offset(leftPad + idx * stepX, latencyToY(pt.avgLatencyMs.toFloat()))
                }
                val peakLatOffsets = points.mapIndexed { idx, pt ->
                    Offset(leftPad + idx * stepX, latencyToY(pt.peakLatencyMs.toFloat()))
                }

                // Smooth Area Fill for Average Latency
                val areaPath = buildSmoothAreaPath(avgLatOffsets, topPad + plotHeight)
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            JissrCyan.copy(alpha = 0.36f),
                            JissrCyan.copy(alpha = 0.03f)
                        ),
                        startY = topPad,
                        endY = topPad + plotHeight
                    )
                )

                // Smooth Stroke for Average Latency
                val linePath = buildSmoothLinePath(avgLatOffsets)
                drawPath(
                    path = linePath,
                    color = JissrCyan,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Peak Latency Spike Line (Amber Dashed/Solid Curve + Spike Stems)
                val peakPath = buildSmoothLinePath(peakLatOffsets)
                drawPath(
                    path = peakPath,
                    color = JissrAmber.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                )

                // Draw Peak Spike Dots & Vertical Range Stems between Avg and Peak Latency
                points.forEachIndexed { idx, pt ->
                    val avgPt = avgLatOffsets[idx]
                    val pkPt = peakLatOffsets[idx]
                    drawLine(
                        color = JissrAmber.copy(alpha = 0.28f),
                        start = pkPt,
                        end = avgPt,
                        strokeWidth = 1.5.dp.toPx()
                    )
                    if (pt.isMajorPeakHour || idx == highestPeakIndex) {
                        drawCircle(
                            color = JissrAmber.copy(alpha = 0.28f),
                            radius = 7.dp.toPx(),
                            center = pkPt
                        )
                        drawCircle(
                            color = JissrAmber,
                            radius = 3.6.dp.toPx(),
                            center = pkPt
                        )
                    }
                }

                // 24h Average Latency Reference Line (Recharts ReferenceLine)
                val avgY = latencyToY(avgLatencyMs.toFloat())
                drawLine(
                    color = JissrCyan.copy(alpha = 0.65f),
                    start = Offset(leftPad, avgY),
                    end = Offset(leftPad + plotWidth, avgY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // ReferenceLine Badge on Left
                val refBadgePaint = Paint(axisTextPaint).apply {
                    color = android.graphics.Color.argb(235, 0, 210, 255)
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "$avgLabel ${avgLatencyMs}ms",
                    leftPad + 6.dp.toPx(),
                    (avgY - 4.dp.toPx()).coerceAtLeast(topPad + 8.dp.toPx()),
                    refBadgePaint
                )

                // Highest Peak Callout Badge on Canvas
                val maxPeakOffset = peakLatOffsets[highestPeakIndex]
                val maxPeakVal = points[highestPeakIndex].peakLatencyMs
                val badgeWidth = 68.dp.toPx()
                val badgeHeight = 16.dp.toPx()
                val badgeX = (maxPeakOffset.x - badgeWidth / 2f).coerceIn(leftPad, leftPad + plotWidth - badgeWidth)
                val badgeY = (maxPeakOffset.y - badgeHeight - 6.dp.toPx()).coerceAtLeast(2.dp.toPx())

                drawRoundRect(
                    color = JissrCrimson.copy(alpha = 0.9f),
                    topLeft = Offset(badgeX, badgeY),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
                val peakBadgePaint = Paint(axisTextPaint).apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    textSize = 8.5.dp.toPx()
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "▲ ${maxPeakVal}ms",
                    badgeX + badgeWidth / 2f,
                    badgeY + badgeHeight - 4.dp.toPx(),
                    peakBadgePaint
                )
            }

            // 3. Draw Average Speed Curve (when DUAL or SPEED_OVER_TIME mode)
            if (viewMode == TrendChartViewMode.DUAL_OVERLAY || viewMode == TrendChartViewMode.SPEED_OVER_TIME) {
                val avgSpeedOffsets = points.mapIndexed { idx, pt ->
                    Offset(leftPad + idx * stepX, speedToY(pt.avgSpeedMbps))
                }
                val peakSpeedOffsets = points.mapIndexed { idx, pt ->
                    Offset(leftPad + idx * stepX, speedToY(pt.peakSpeedMbps))
                }

                if (viewMode == TrendChartViewMode.SPEED_OVER_TIME) {
                    val speedArea = buildSmoothAreaPath(avgSpeedOffsets, topPad + plotHeight)
                    drawPath(
                        path = speedArea,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                JissrEmerald.copy(alpha = 0.35f),
                                JissrEmerald.copy(alpha = 0.03f)
                            ),
                            startY = topPad,
                            endY = topPad + plotHeight
                        )
                    )

                    val peakSpeedLine = buildSmoothLinePath(peakSpeedOffsets)
                    drawPath(
                        path = peakSpeedLine,
                        color = JissrCyan.copy(alpha = 0.8f),
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)
                        )
                    )

                    val avgSpeedY = speedToY(avgSpeedMbps)
                    drawLine(
                        color = JissrEmerald.copy(alpha = 0.65f),
                        start = Offset(leftPad, avgSpeedY),
                        end = Offset(leftPad + plotWidth, avgSpeedY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                val speedLinePath = buildSmoothLinePath(avgSpeedOffsets)
                drawPath(
                    path = speedLinePath,
                    color = JissrEmerald,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 4. Draw Interactive Scrubber Crosshair & Active Data Dots (Recharts Tooltip Cursor)
            val safeIdx = selectedIndex.coerceIn(0, points.lastIndex)
            val selectedX = leftPad + safeIdx * stepX
            val selectedPt = points[safeIdx]

            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(selectedX, topPad),
                end = Offset(selectedX, topPad + plotHeight),
                strokeWidth = 1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f), 0f)
            )

            if (viewMode != TrendChartViewMode.SPEED_OVER_TIME) {
                val latY = latencyToY(selectedPt.avgLatencyMs.toFloat())
                val pkY = latencyToY(selectedPt.peakLatencyMs.toFloat())

                drawCircle(
                    color = JissrCyan.copy(alpha = 0.32f),
                    radius = 8.dp.toPx(),
                    center = Offset(selectedX, latY)
                )
                drawCircle(
                    color = JissrCyan,
                    radius = 4.5.dp.toPx(),
                    center = Offset(selectedX, latY)
                )

                drawCircle(
                    color = JissrAmber.copy(alpha = 0.32f),
                    radius = 7.dp.toPx(),
                    center = Offset(selectedX, pkY)
                )
                drawCircle(
                    color = JissrAmber,
                    radius = 4.dp.toPx(),
                    center = Offset(selectedX, pkY)
                )
            }

            if (viewMode != TrendChartViewMode.LATENCY_PEAKS) {
                val spdY = speedToY(selectedPt.avgSpeedMbps)
                drawCircle(
                    color = JissrEmerald.copy(alpha = 0.32f),
                    radius = 8.dp.toPx(),
                    center = Offset(selectedX, spdY)
                )
                drawCircle(
                    color = JissrEmerald,
                    radius = 4.5.dp.toPx(),
                    center = Offset(selectedX, spdY)
                )
            }
        }
    }
}

private fun buildSmoothLinePath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val current = points[i]
        val next = points[i + 1]
        val controlX = (current.x + next.x) / 2f
        path.cubicTo(
            controlX,
            current.y,
            controlX,
            next.y,
            next.x,
            next.y
        )
    }
    return path
}

private fun buildSmoothAreaPath(points: List<Offset>, baselineY: Float): Path {
    val path = buildSmoothLinePath(points)
    if (points.isNotEmpty()) {
        path.lineTo(points.last().x, baselineY)
        path.lineTo(points.first().x, baselineY)
        path.close()
    }
    return path
}

@Composable
private fun LegendDotItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun build24HourTunnelSeries(
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    speedTestProgress: SpeedTestProgress,
    isArabic: Boolean
): List<HourlyTunnelPoint> {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

    val nodeHash = abs((activeNode?.id ?: tunnelState.activeNodeId).hashCode())
    val baseLatency = (activeNode?.lastLatencyMs?.takeIf { it > 0 } ?: 28).coerceIn(14, 165)

    val modeLatencyModifier = when (tunnelState.smartMode) {
        SmartBridgeMode.GAMING -> -4
        SmartBridgeMode.STEALTH -> 2
        SmartBridgeMode.STREAMING -> 0
        SmartBridgeMode.DOUBLE_HOP -> 14
    }

    val modeSpeedMultiplier = when (tunnelState.smartMode) {
        SmartBridgeMode.STREAMING -> 1.14f
        SmartBridgeMode.GAMING -> 1.06f
        SmartBridgeMode.STEALTH -> 1.0f
        SmartBridgeMode.DOUBLE_HOP -> 0.88f
    }

    val measuredSpeedBase = speedTestProgress.downloadMbps.takeIf { it > 20f } ?: 168.5f

    return (0 until 24).map { idx ->
        val hoursAgo = 23 - idx
        val slotHour = (currentHour - hoursAgo + 24) % 24
        val timeLabel = if (idx == 23) {
            if (isArabic) "الآن" else "Now"
        } else {
            String.format(Locale.US, "%02d:00", slotHour)
        }

        // Diurnal network traffic congestion curve (evening hours 19:00-23:00 have higher peak latency)
        val eveningLoadWave = when (slotHour) {
            in 19..23 -> 9 + ((slotHour + nodeHash) % 7)
            in 13..18 -> 4 + ((slotHour + nodeHash) % 5)
            in 1..6 -> -3
            else -> 1 + ((slotHour + nodeHash) % 4)
        }

        val harmonic = (sin((idx + (nodeHash % 11)) * 0.65) * 4.5).roundToInt()
        val avgLat = (baseLatency + modeLatencyModifier + eveningLoadWave + harmonic).coerceAtLeast(11)

        // Realistic peak spikes on busy hours
        val isSpikeHour = (slotHour == 21 || slotHour == 14 || (idx + nodeHash) % 7 == 0)
        val spikeDelta = if (isSpikeHour) {
            16 + ((idx * 7 + nodeHash) % 22)
        } else {
            6 + ((idx * 3 + nodeHash) % 9)
        }
        val peakLat = if (idx == 23 && speedTestProgress.pingMs > 0) {
            max(avgLat + 6, speedTestProgress.pingMs + speedTestProgress.jitterMs * 2)
        } else {
            avgLat + spikeDelta
        }

        // Average & Peak Speed in Mbps inversely correlated with congestion spikes
        val speedWave = (cos((idx + (nodeHash % 7)) * 0.52) * 18.0).toFloat()
        val congestionDip = if (isSpikeHour) -14.5f else 4.0f
        val computedAvgSpeed = if (idx == 23 && speedTestProgress.downloadMbps > 0f) {
            speedTestProgress.downloadMbps
        } else {
            ((measuredSpeedBase * modeSpeedMultiplier) + speedWave + congestionDip).coerceIn(42f, 245f)
        }

        val computedPeakSpeed = (computedAvgSpeed * (1.14f + ((idx + nodeHash) % 5) * 0.025f)).coerceAtMost(250f)

        HourlyTunnelPoint(
            index = idx,
            timeLabel = timeLabel,
            avgLatencyMs = if (idx == 23 && speedTestProgress.pingMs > 0) speedTestProgress.pingMs else avgLat,
            peakLatencyMs = peakLat,
            avgSpeedMbps = computedAvgSpeed,
            peakSpeedMbps = computedPeakSpeed,
            isMajorPeakHour = isSpikeHour
        )
    }
}
