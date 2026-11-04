package com.example.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.JissrStrings
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrAmber
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.vpn.VpnTunnelState

data class InstalledAppEntry(
    val appName: String,
    val packageName: String,
    val categoryEmoji: String
)

@Composable
fun ShieldAndAppsScreen(
    strings: JissrStrings,
    tunnelState: VpnTunnelState,
    onUpdateOption: (
        splitLan: Boolean?,
        dnsGuard: Boolean?,
        stealth: Boolean?,
        adBlock: Boolean?,
        killSwitch: Boolean?,
        ipv6Leak: Boolean?,
        mtu: Int?,
        protocol: String?
    ) -> Unit,
    onToggleAppBypass: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val installedApps = remember(context) {
        val pm = context.packageManager
        val dynamicApps = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("chrome") || it.packageName.contains("youtube") }
                .take(8)
                .map { info ->
                    val label = try {
                        pm.getApplicationLabel(info).toString()
                    } catch (_: Exception) {
                        info.packageName.substringAfterLast('.')
                    }
                    InstalledAppEntry(
                        appName = label,
                        packageName = info.packageName,
                        categoryEmoji = "📱"
                    )
                }
        } catch (_: Exception) {
            emptyList()
        }

        val curatedEssentials = listOf(
            InstalledAppEntry("Chrome Browser · متصفح كروم", "com.android.chrome", "🌐"),
            InstalledAppEntry("Telegram · تيليجرام", "org.telegram.messenger", "✈️"),
            InstalledAppEntry("WhatsApp · واتساب", "com.whatsapp", "💬"),
            InstalledAppEntry("YouTube 4K · يوتيوب", "com.google.android.youtube", "🎬"),
            InstalledAppEntry("PUBG Mobile · ببجي موبايل", "com.tencent.ig", "🎮"),
            InstalledAppEntry("Discord · ديسكورد", "com.discord", "🎧"),
            InstalledAppEntry("Mobile Banking · تطبيق البنك", "com.local.banking.app", "🏦")
        )
        (dynamicApps + curatedEssentials).distinctBy { it.packageName }
    }

    val protocols = listOf("WireGuard-Jissr", "VLESS-Reality", "Hysteria2-QUIC", "Shadowsocks-2022")
    val mtuOptions = listOf(1280, 1360, 1380, 1400, 1420, 1500)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shield_apps_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cyber Shield Header & Security Score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GppGood,
                                contentDescription = null,
                                tint = JissrEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.shieldSuiteTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.shieldSuiteSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianCardElevated
                        ) {
                            Text(
                                text = "🛡️ ${tunnelState.blockedTrackersCount} ${strings.blockedThreatsLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = JissrCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = JissrEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(2.5.dp, JissrEmerald),
                        modifier = Modifier.size(78.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${tunnelState.securityScorePercent}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 20.sp
                                ),
                                color = JissrEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.securityScoreLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Advanced Cyber Shield Toggles
        item {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShieldToggleItem(
                        icon = Icons.Filled.Block,
                        iconTint = JissrEmerald,
                        title = strings.adBlockTitle,
                        subtitle = strings.adBlockSubtitle,
                        checked = tunnelState.adBlockerEnabled,
                        tag = "shield_toggle_adblock",
                        onCheckedChange = {
                            onUpdateOption(null, null, null, it, null, null, null, null)
                        }
                    )

                    ShieldToggleItem(
                        icon = Icons.Filled.PowerOff,
                        iconTint = JissrAmber,
                        title = strings.killSwitchTitle,
                        subtitle = strings.killSwitchSubtitle,
                        checked = tunnelState.killSwitchEnabled,
                        tag = "shield_toggle_killswitch",
                        onCheckedChange = {
                            onUpdateOption(null, null, null, null, it, null, null, null)
                        }
                    )

                    ShieldToggleItem(
                        icon = Icons.Filled.VisibilityOff,
                        iconTint = JissrCyan,
                        title = strings.dpiStealthTitle,
                        subtitle = strings.dpiStealthSubtitle,
                        checked = tunnelState.stealthPaddingEnabled,
                        tag = "shield_toggle_dpi",
                        onCheckedChange = {
                            onUpdateOption(null, null, it, null, null, null, null, null)
                        }
                    )

                    ShieldToggleItem(
                        icon = Icons.Filled.AdminPanelSettings,
                        iconTint = JissrEmerald,
                        title = strings.ipv6LeakTitle,
                        subtitle = strings.ipv6LeakSubtitle,
                        checked = tunnelState.ipv6LeakProtectionEnabled,
                        tag = "shield_toggle_ipv6",
                        onCheckedChange = {
                            onUpdateOption(null, null, null, null, null, it, null, null)
                        }
                    )

                    ShieldToggleItem(
                        icon = Icons.Filled.Router,
                        iconTint = JissrCyan,
                        title = strings.splitTunnelLanTitle,
                        subtitle = strings.splitTunnelLanSubtitle,
                        checked = tunnelState.splitTunnelBypassLan,
                        tag = "shield_toggle_lan",
                        onCheckedChange = {
                            onUpdateOption(it, null, null, null, null, null, null, null)
                        }
                    )
                }
            }
        }

        // 3. Protocol & MTU Tuning Card
        item {
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
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = JissrCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.protocolTuningTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(protocols) { proto ->
                            val selected = tunnelState.activeProtocol == proto
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateOption(null, null, null, null, null, null, null, proto)
                                },
                                label = { Text(proto) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JissrEmerald.copy(alpha = 0.2f),
                                    selectedLabelColor = JissrEmerald
                                )
                            )
                        }
                    }

                    Text(
                        text = "${strings.mtuTitle}: ${tunnelState.mtu} bytes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mtuOptions) { mtuVal ->
                            val selected = tunnelState.mtu == mtuVal
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateOption(null, null, null, null, null, null, mtuVal, null)
                                },
                                label = {
                                    Text(
                                        text = "$mtuVal",
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JissrCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = JissrCyan
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Per-App Split Tunneling Header & List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.splitAppsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = strings.splitAppsSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(installedApps, key = { it.packageName }) { app ->
            val isBypassed = tunnelState.bypassedPackages.contains(app.packageName)
            val isRoutedInVpn = !isBypassed

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAppBypass(app.packageName) }
                    .testTag("split_app_${app.packageName}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRoutedInVpn) ObsidianSurface else ObsidianSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (isRoutedInVpn) JissrEmerald.copy(alpha = 0.35f) else BorderSubtleDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianCardElevated,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = app.categoryEmoji, fontSize = 20.sp)
                            }
                        }

                        Column {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRoutedInVpn) JissrEmerald.copy(alpha = 0.16f) else JissrAmber.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = if (isRoutedInVpn) strings.appRoutedInTunnel else strings.appBypassedDirect,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRoutedInVpn) JissrEmerald else JissrAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Switch(
                            checked = isRoutedInVpn,
                            onCheckedChange = { onToggleAppBypass(app.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF032219),
                                checkedTrackColor = JissrEmerald
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShieldToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).padding(end = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF032219),
                checkedTrackColor = JissrEmerald,
                uncheckedTrackColor = ObsidianSurfaceVariant
            )
        )
    }
}
