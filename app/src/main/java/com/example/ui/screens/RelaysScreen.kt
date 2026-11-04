package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun RelaysScreen(
    strings: JissrStrings,
    nodes: List<BridgeNodeEntity>,
    activeNodeId: String,
    selectedRegion: String,
    searchQuery: String,
    isProbing: Boolean,
    onRegionSelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onProbeAll: () -> Unit,
    onSelectNode: (BridgeNodeEntity) -> Unit,
    onToggleFavorite: (BridgeNodeEntity) -> Unit,
    onDeleteCustomNode: (BridgeNodeEntity) -> Unit,
    onAddCustomNode: (String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddModal by remember { mutableStateOf(false) }

    val regions = remember(strings) {
        listOf(
            "All" to strings.regionAll,
            "Favorites" to strings.regionFavorites,
            "Middle East" to strings.regionMiddleEast,
            "Europe" to strings.regionEurope,
            "Asia" to strings.regionAsia,
            "Americas" to strings.regionAmericas,
            "Custom" to strings.regionCustom
        )
    }

    val filteredNodes = remember(nodes, selectedRegion, searchQuery) {
        nodes.filter { node ->
            val matchesRegion = when (selectedRegion) {
                "All" -> true
                "Favorites" -> node.isFavorite
                "Custom" -> node.isCustom
                else -> node.region.equals(selectedRegion, ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                node.city.contains(searchQuery, ignoreCase = true) ||
                node.cityAr.contains(searchQuery, ignoreCase = true) ||
                node.country.contains(searchQuery, ignoreCase = true) ||
                node.countryAr.contains(searchQuery, ignoreCase = true) ||
                node.protocol.contains(searchQuery, ignoreCase = true) ||
                node.endpointHost.contains(searchQuery, ignoreCase = true)
            matchesRegion && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onProbeAll,
                enabled = !isProbing,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("probe_latencies_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrEmerald,
                    contentColor = Color(0xFF032219)
                )
            ) {
                if (isProbing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF032219)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.probingRtt, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.probeAllRelays, style = MaterialTheme.typography.labelLarge)
                }
            }

            OutlinedButton(
                onClick = { showAddModal = true },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_custom_node_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JissrCyan)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = strings.addUriOrNode,
                    tint = JissrCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.addUriOrNode, style = MaterialTheme.typography.labelLarge, color = JissrCyan)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("relay_search_input"),
            placeholder = { Text(strings.searchPlaceholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(regions) { (key, label) ->
                val selected = selectedRegion == key
                FilterChip(
                    selected = selected,
                    onClick = { onRegionSelected(key) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("region_chip_${key.lowercase().replace(" ", "_")}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JissrEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = JissrEmerald
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) JissrEmerald else BorderSubtleDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredNodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.NetworkCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (strings.isArabic) "لا توجد خوادم مطابقة للبحث" else "No matching bridge relays found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("relays_list"),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNodes, key = { it.id }) { node ->
                    RelayNodeCard(
                        strings = strings,
                        node = node,
                        isSelected = node.id == activeNodeId,
                        onSelect = { onSelectNode(node) },
                        onToggleFavorite = { onToggleFavorite(node) },
                        onDeleteCustom = { onDeleteCustomNode(node) }
                    )
                }
            }
        }
    }

    if (showAddModal) {
        AddCustomBridgeDialog(
            isArabic = strings.isArabic,
            onDismiss = { showAddModal = false },
            onConfirm = { label, country, hostOrUri, port, protocol, obfuscation ->
                onAddCustomNode(label, country, hostOrUri, port, protocol, obfuscation)
                showAddModal = false
            }
        )
    }
}

@Composable
private fun RelayNodeCard(
    strings: JissrStrings,
    node: BridgeNodeEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCustom: () -> Unit
) {
    val latencyColor = when {
        node.lastLatencyMs <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        node.lastLatencyMs < 65 -> JissrEmerald
        node.lastLatencyMs < 140 -> JissrCyan
        else -> JissrAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("relay_card_${node.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ObsidianCardElevated else ObsidianSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) JissrEmerald else BorderSubtleDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceVariant,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = node.flagEmoji, fontSize = 22.sp)
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = node.localizedCity(strings.isArabic),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "· ${node.localizedCountry(strings.isArabic)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = JissrEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = node.protocol,
                                style = MaterialTheme.typography.labelSmall,
                                color = JissrCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${node.serverLoadPercent}% Load",
                                style = MaterialTheme.typography.labelSmall,
                                color = JissrEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${node.endpointHost}:${node.endpointPort}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = latencyColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, latencyColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (node.lastLatencyMs > 0) "${node.lastLatencyMs} ms" else "-- ms",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = latencyColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("fav_node_${node.id}")
                ) {
                    Icon(
                        imageVector = if (node.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (node.isFavorite) JissrCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (node.isCustom) {
                    IconButton(
                        onClick = onDeleteCustom,
                        modifier = Modifier.testTag("delete_node_${node.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = JissrCrimson
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomBridgeDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit
) {
    var cityOrLabel by remember { mutableStateOf("") }
    var country by remember { mutableStateOf(if (isArabic) "خادم مخصص" else "Custom Bridge") }
    var hostOrUri by remember { mutableStateOf("vless://jissr@1.1.1.1:443#Bahrain-Edge") }
    var port by remember { mutableStateOf("443") }
    var selectedProtocol by remember { mutableStateOf("WireGuard-Jissr") }
    val selectedObfuscation by remember { mutableStateOf("Stealth TLS 1.3") }

    val protocols = listOf("WireGuard-Jissr", "VLESS-Reality", "Hysteria2-QUIC", "Shadowsocks-2022")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = if (isArabic) "إضافة خادم مخصص أو رابط جسر" else "Add Custom Bridge / Import URI",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) {
                        "ألصق رابط vless:// أو wg:// أو hy2:// أو أدخل عنوان IP والمنفذ ليتم فحصه وإضافته فوراً."
                    } else {
                        "Paste a vless://, wg://, hy2://, ss:// URI or enter a host IP/domain to probe and add."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = hostOrUri,
                    onValueChange = { hostOrUri = it },
                    label = { Text(if (isArabic) "الرابط أو عنوان IP" else "Bridge URI or Host IP") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_node_uri_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cityOrLabel,
                        onValueChange = { cityOrLabel = it },
                        label = { Text(if (isArabic) "اسم الخادم (اختياري)" else "Node Label") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_node_label_input")
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text(if (isArabic) "المنفذ" else "Port") },
                        singleLine = true,
                        modifier = Modifier.width(92.dp)
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(protocols) { proto ->
                        FilterChip(
                            selected = selectedProtocol == proto,
                            onClick = { selectedProtocol = proto },
                            label = { Text(proto, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        cityOrLabel,
                        country,
                        hostOrUri,
                        port,
                        selectedProtocol,
                        selectedObfuscation
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrEmerald,
                    contentColor = Color(0xFF032219)
                ),
                modifier = Modifier.testTag("confirm_add_node_button")
            ) {
                Text(if (isArabic) "إضافة وفحص" else "Add & Probe")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
