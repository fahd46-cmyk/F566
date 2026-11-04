package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BridgeNodeEntity
import com.example.data.local.TunnelAuditLogEntity
import com.example.network.DnsResolverBenchmark
import com.example.network.LiveNetworkTrace
import com.example.network.SpeedTestProgress
import com.example.ui.PortProbeResult
import com.example.ui.components.Tunnel24HourTrendCard
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
import com.example.vpn.VpnTunnelState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    activeNode: BridgeNodeEntity?,
    speedTestProgress: SpeedTestProgress,
    networkTrace: LiveNetworkTrace,
    dnsBenchmarks: List<DnsResolverBenchmark>,
    portProbeResult: PortProbeResult,
    activeDnsServer: String,
    isRunningTrace: Boolean,
    auditLogs: List<TunnelAuditLogEntity>,
    onRunTrace: () -> Unit,
    onProbeHostPort: (String, Int) -> Unit,
    onApplyDnsServer: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("diagnostics_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Tunnel24HourTrendCard(
                strings = strings,
                tunnelState = tunnelState,
                activeNode = activeNode,
                speedTestProgress = speedTestProgress
            )
        }

        item {
            LiveEdgeTraceCard(
                strings = strings,
                trace = networkTrace,
                isRunning = isRunningTrace,
                onRunTrace = onRunTrace
            )
        }

        item {
            InteractivePortTesterCard(
                strings = strings,
                probeResult = portProbeResult,
                onProbeHostPort = onProbeHostPort
            )
        }

        item {
            DnsBenchmarkSectionCard(
                strings = strings,
                benchmarks = dnsBenchmarks,
                activeDnsServer = activeDnsServer,
                onApplyDnsServer = onApplyDnsServer
            )
        }

        item {
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
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.auditLogTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (auditLogs.isNotEmpty()) {
                    TextButton(
                        onClick = onClearLogs,
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = strings.clearLogsBtn,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.clearLogsBtn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(auditLogs, key = { it.id }) { log ->
            AuditLogItemCard(log = log)
        }
    }
}

@Composable
private fun LiveEdgeTraceCard(
    strings: JissrStrings,
    trace: LiveNetworkTrace,
    isRunning: Boolean,
    onRunTrace: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = strings.liveTraceTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.liveTraceSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (trace.isSuccess) JissrEmerald.copy(alpha = 0.16f) else JissrAmber.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (trace.isSuccess) "TLS 1.3 LIVE" else "LOCAL TUN",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trace.isSuccess) JissrEmerald else JissrAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryMetricTile(
                    label = strings.publicIpLabel,
                    value = trace.publicIp,
                    accent = JissrEmerald,
                    modifier = Modifier.weight(1.3f)
                )
                TelemetryMetricTile(
                    label = strings.edgeColoLabel,
                    value = "${trace.edgeColo} (${trace.countryCode})",
                    accent = JissrCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryMetricTile(
                    label = strings.tlsCipherLabel,
                    value = "${trace.tlsVersion} · ${trace.httpProtocol}",
                    accent = JissrCyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricTile(
                    label = strings.dnsLookupLabel,
                    value = "${trace.dnsLookupMs} ms",
                    accent = JissrEmerald,
                    modifier = Modifier.weight(0.75f)
                )
                TelemetryMetricTile(
                    label = strings.traceRttLabel,
                    value = "${trace.traceLatencyMs} ms",
                    accent = JissrEmerald,
                    modifier = Modifier.weight(0.75f)
                )
            }

            Button(
                onClick = onRunTrace,
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("run_trace_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrCyan,
                    contentColor = Color(0xFF042233)
                )
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF042233)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.runningTraceBtn, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.runLiveTraceBtn, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun InteractivePortTesterCard(
    strings: JissrStrings,
    probeResult: PortProbeResult,
    onProbeHostPort: (String, Int) -> Unit
) {
    var hostInput by remember { mutableStateOf(probeResult.host) }
    var portInput by remember { mutableStateOf(probeResult.port.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NetworkPing,
                    contentDescription = null,
                    tint = JissrEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = strings.portTesterTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.portTesterSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hostInput,
                    onValueChange = { hostInput = it },
                    label = { Text("Host / IP") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("port_tester_host_input")
                )
                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    label = { Text("Port") },
                    singleLine = true,
                    modifier = Modifier
                        .width(88.dp)
                        .testTag("port_tester_port_input")
                )
                Button(
                    onClick = {
                        onProbeHostPort(hostInput, portInput.toIntOrNull() ?: 443)
                    },
                    enabled = !probeResult.isProbing,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("port_tester_run_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JissrEmerald,
                        contentColor = Color(0xFF032219)
                    )
                ) {
                    Text(strings.testPortBtn, style = MaterialTheme.typography.labelLarge)
                }
            }

            probeResult.latencyMs?.let { rtt ->
                val isReachable = rtt > 0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isReachable) JissrEmerald.copy(alpha = 0.14f) else JissrCrimson.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${probeResult.host}:${probeResult.port}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isReachable) "OPEN · $rtt ms RTT" else "FILTERED / TIMEOUT",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = if (isReachable) JissrEmerald else JissrCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DnsBenchmarkSectionCard(
    strings: JissrStrings,
    benchmarks: List<DnsResolverBenchmark>,
    activeDnsServer: String,
    onApplyDnsServer: (String) -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Dns,
                    contentDescription = null,
                    tint = JissrEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = strings.dnsBenchmarkTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            benchmarks.forEach { item ->
                val isCurrent = item.primaryIp == activeDnsServer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrent) ObsidianCardElevated else ObsidianSurfaceVariant,
                    border = if (isCurrent) BorderStroke(1.dp, JissrEmerald) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.providerName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.primaryIp} · ${item.protocolLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (item.latencyMs > 0) "${item.latencyMs} ms" else "-- ms",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = JissrEmerald,
                                fontWeight = FontWeight.Bold
                            )

                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = JissrEmerald.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = JissrEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = strings.activeBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JissrEmerald
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onApplyDnsServer(item.primaryIp) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("apply_dns_${item.primaryIp}")
                                ) {
                                    Text(strings.useDnsBtn, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogItemCard(log: TunnelAuditLogEntity) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ObsidianCardElevated,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.eventTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${log.nodeName} · ${log.protocol}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JissrCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
