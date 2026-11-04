package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BridgeNodeEntity
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
import com.example.vpn.TunnelConnectionStatus
import com.example.vpn.VpnTunnelState

@Composable
fun QuickControlFabMenu(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenServerSwitchSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = tunnelState.status == TunnelConnectionStatus.CONNECTED

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Switch Server Location Pill
                QuickFabMenuItem(
                    label = strings.quickSwitchServerAction,
                    sublabel = "${tunnelState.activeFlag} ${tunnelState.displayCity(strings.isArabic)}",
                    icon = Icons.Filled.SwapHoriz,
                    accentColor = JissrCyan,
                    testTag = "fab_switch_server_sheet",
                    onClick = {
                        onToggleExpand()
                        onOpenServerSwitchSheet()
                    }
                )

                // 2. Disconnect Pill
                QuickFabMenuItem(
                    label = strings.quickDisconnectAction,
                    sublabel = if (isConnected) "TUN ${tunnelState.virtualIp}" else null,
                    icon = Icons.Filled.PowerSettingsNew,
                    accentColor = JissrCrimson,
                    testTag = "fab_quick_disconnect",
                    onClick = {
                        onToggleExpand()
                        onDisconnect()
                    }
                )

                // 3. Connect Pill
                QuickFabMenuItem(
                    label = strings.quickConnectAction,
                    sublabel = tunnelState.activeProtocol,
                    icon = Icons.Filled.Bolt,
                    accentColor = JissrEmerald,
                    testTag = "fab_quick_connect",
                    onClick = {
                        onToggleExpand()
                        onConnect()
                    }
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = onToggleExpand,
            containerColor = if (isExpanded) ObsidianCardElevated else if (isConnected) JissrEmerald else JissrCyan,
            contentColor = if (isExpanded) JissrCyan else Color(0xFF032219),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.testTag("quick_action_fab"),
            icon = {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Bolt,
                    contentDescription = strings.quickMenuLabel
                )
            },
            text = {
                Text(
                    text = "${tunnelState.activeFlag} ${strings.quickMenuLabel}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

@Composable
private fun QuickFabMenuItem(
    label: String,
    sublabel: String?,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.65f)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (!sublabel.isNullOrBlank()) {
                    Text(
                        text = sublabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.18f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickServerSwitchBottomSheet(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    nodes: List<BridgeNodeEntity>,
    onDismiss: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSmartFastestConnect: () -> Unit,
    onSelectServerAndConnect: (BridgeNodeEntity) -> Unit,
    onOpenAllRelaysTab: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isConnected = tunnelState.status == TunnelConnectionStatus.CONNECTED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("quick_server_switch_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Title & Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.quickSheetTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.quickSheetSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isConnected) JissrEmerald.copy(alpha = 0.16f) else JissrAmber.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, if (isConnected) JissrEmerald else JissrAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) JissrEmerald else JissrAmber)
                        )
                        Text(
                            text = if (isConnected) "ON · ${tunnelState.virtualIp}" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = if (isConnected) JissrEmerald else JissrAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Connect & Disconnect Toggle Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onConnect()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_connect_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JissrEmerald,
                        contentColor = Color(0xFF032219)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.quickConnectAction,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = {
                        onDisconnect()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_disconnect_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, JissrCrimson)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = null,
                        tint = JissrCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.quickDisconnectAction,
                        style = MaterialTheme.typography.labelLarge,
                        color = JissrCrimson
                    )
                }
            }

            // Smart Fastest Server Auto-Switch Button
            OutlinedButton(
                onClick = {
                    onSmartFastestConnect()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("sheet_smart_fastest_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JissrCyan)
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

            // Server Locations List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = strings.quickSheetSelectLocation,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        onOpenAllRelaysTab()
                    },
                    modifier = Modifier.testTag("sheet_open_all_relays_button")
                ) {
                    Text(
                        text = strings.quickSheetOpenAllRelays,
                        style = MaterialTheme.typography.labelSmall,
                        color = JissrCyan
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 310.dp)
                    .testTag("sheet_server_list"),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nodes, key = { it.id }) { node ->
                    val isSelected = node.id == tunnelState.activeNodeId
                    val pingColor = when {
                        node.lastLatencyMs <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                        node.lastLatencyMs < 65 -> JissrEmerald
                        node.lastLatencyMs < 140 -> JissrCyan
                        else -> JissrAmber
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectServerAndConnect(node)
                                onDismiss()
                            }
                            .testTag("sheet_server_item_${node.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ObsidianCardElevated else ObsidianSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) JissrEmerald else BorderSubtleDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = node.flagEmoji, fontSize = 22.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = node.localizedCity(strings.isArabic),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "· ${node.localizedCountry(strings.isArabic)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${node.protocol} · ${node.obfuscationMode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JissrCyan
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = pingColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (node.lastLatencyMs > 0) "${node.lastLatencyMs} ms" else "-- ms",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = pingColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = JissrEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
