package com.example

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.local.JissrDatabase
import com.example.data.repository.JissrRepository
import com.example.network.NetworkDiagnosticsService
import com.example.ui.JissrTab
import com.example.ui.JissrViewModel
import com.example.ui.components.QuickControlFabMenu
import com.example.ui.components.QuickServerSwitchBottomSheet
import com.example.ui.i18n.JissrLocalization
import com.example.ui.i18n.JissrStrings
import com.example.ui.screens.BridgeCockpitScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.RelaysScreen
import com.example.ui.screens.ShieldAndAppsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.vpn.TunnelConnectionStatus
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val repository = remember {
                    JissrRepository(JissrDatabase.getInstance(context).jissrDao())
                }
                val diagnosticsService = remember { NetworkDiagnosticsService() }
                val viewModel: JissrViewModel = viewModel(
                    factory = JissrViewModel.provideFactory(repository, diagnosticsService)
                )
                JissrVpnApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavDestination(
    val tab: JissrTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun JissrVpnApp(viewModel: JissrViewModel) {
    val context = LocalContext.current
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val strings = if (isArabic) JissrLocalization.Arabic else JissrLocalization.English
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val tunnelState by viewModel.tunnelState.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegionFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isProbing by viewModel.isProbingLatencies.collectAsStateWithLifecycle()
    val isRunningTrace by viewModel.isRunningTrace.collectAsStateWithLifecycle()
    val networkTrace by viewModel.networkTrace.collectAsStateWithLifecycle()
    val dnsBenchmarks by viewModel.dnsBenchmarks.collectAsStateWithLifecycle()
    val speedTestProgress by viewModel.speedTestProgress.collectAsStateWithLifecycle()
    val portProbeResult by viewModel.portProbeResult.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val studioState by viewModel.studioState.collectAsStateWithLifecycle()
    val wgDraft by viewModel.wgDraft.collectAsStateWithLifecycle()
    val aiOptimizationState by viewModel.aiOptimizationState.collectAsStateWithLifecycle()
    val selectedAiGoal by viewModel.selectedAiGoal.collectAsStateWithLifecycle()

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showQuickServerSheet by remember { mutableStateOf(false) }

    val activeNode = remember(nodes, tunnelState.activeNodeId) {
        nodes.find { it.id == tunnelState.activeNodeId } ?: nodes.firstOrNull()
    }

    BackHandler(enabled = currentTab != JissrTab.BRIDGE) {
        viewModel.selectTab(JissrTab.BRIDGE)
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpnService(context)
        } else {
            viewModel.startVpnService(context)
        }
    }

    val onConnectVpn: () -> Unit = {
        val prepareIntent = try {
            VpnService.prepare(context)
        } catch (_: Exception) {
            null
        }
        if (prepareIntent != null) {
            try {
                vpnPermissionLauncher.launch(prepareIntent)
            } catch (_: Exception) {
                viewModel.startVpnService(context)
            }
        } else {
            viewModel.startVpnService(context)
        }
    }

    val onDisconnectVpn: () -> Unit = {
        viewModel.stopVpnService(context)
    }

    val onToggleVpnConnection: () -> Unit = {
        if (tunnelState.status == TunnelConnectionStatus.CONNECTED ||
            tunnelState.status == TunnelConnectionStatus.CONNECTING
        ) {
            onDisconnectVpn()
        } else {
            onConnectVpn()
        }
    }

    val destinations = remember(strings) {
        listOf(
            NavDestination(
                tab = JissrTab.BRIDGE,
                label = strings.navBridge,
                selectedIcon = Icons.Filled.Shield,
                unselectedIcon = Icons.Outlined.Shield,
                testTag = "nav_tab_bridge"
            ),
            NavDestination(
                tab = JissrTab.RELAYS,
                label = strings.navRelays,
                selectedIcon = Icons.Filled.Public,
                unselectedIcon = Icons.Outlined.Public,
                testTag = "nav_tab_relays"
            ),
            NavDestination(
                tab = JissrTab.SHIELD,
                label = strings.navShield,
                selectedIcon = Icons.Filled.AdminPanelSettings,
                unselectedIcon = Icons.Outlined.AdminPanelSettings,
                testTag = "nav_tab_shield"
            ),
            NavDestination(
                tab = JissrTab.DIAGNOSTICS,
                label = strings.navDiagnostics,
                selectedIcon = Icons.Filled.Radar,
                unselectedIcon = Icons.Outlined.Radar,
                testTag = "nav_tab_diagnostics"
            ),
            NavDestination(
                tab = JissrTab.STUDIO,
                label = strings.navStudio,
                selectedIcon = Icons.Filled.Tune,
                unselectedIcon = Icons.Outlined.Tune,
                testTag = "nav_tab_studio"
            )
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF050B14),
                            ObsidianBg,
                            Color(0xFF091422)
                        )
                    )
                )
        ) {
            val isWideScreen = maxWidth >= 700.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    JissrTopHeader(
                        strings = strings,
                        isConnected = tunnelState.status == TunnelConnectionStatus.CONNECTED,
                        activeCity = tunnelState.displayCity(isArabic),
                        activeFlag = tunnelState.activeFlag,
                        peakDownloadMbps = speedTestProgress.downloadMbps,
                        onOpenQuickServerSheet = { showQuickServerSheet = true },
                        onToggleLanguage = viewModel::toggleLanguage
                    )
                },
                floatingActionButton = {
                    QuickControlFabMenu(
                        strings = strings,
                        tunnelState = tunnelState,
                        isExpanded = isFabMenuExpanded,
                        onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                        onConnect = onConnectVpn,
                        onDisconnect = onDisconnectVpn,
                        onOpenServerSwitchSheet = { showQuickServerSheet = true }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        JissrGlassNavigationDock(
                            destinations = destinations,
                            currentTab = currentTab,
                            onSelectTab = viewModel::selectTab
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = ObsidianSurface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            destinations.forEach { dest ->
                                val selected = currentTab == dest.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(dest.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = JissrEmerald,
                                        selectedTextColor = JissrEmerald,
                                        indicatorColor = JissrEmerald.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val contentModifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)

                        when (currentTab) {
                            JissrTab.BRIDGE -> {
                                BridgeCockpitScreen(
                                    strings = strings,
                                    tunnelState = tunnelState,
                                    activeNode = activeNode,
                                    speedTestProgress = speedTestProgress,
                                    aiState = aiOptimizationState,
                                    selectedAiGoal = selectedAiGoal,
                                    onSelectAiGoal = viewModel::selectAiGoal,
                                    onRunAiOptimization = viewModel::runAiTunnelOptimization,
                                    onApplyAiPlan = { plan ->
                                        viewModel.applyAiOptimizationPlan(plan, context)
                                    },
                                    onStreamCustomAiQuestion = viewModel::streamCustomAiQuestion,
                                    onToggleConnection = onToggleVpnConnection,
                                    onSmartFastestConnect = {
                                        viewModel.selectFastestNodeAndConnect(context)
                                    },
                                    onRunSpeedTest = viewModel::runInternetSpeedTest,
                                    onSelectSmartMode = viewModel::selectSmartMode,
                                    onOpenRelayPicker = { showQuickServerSheet = true },
                                    modifier = contentModifier
                                )
                            }

                            JissrTab.RELAYS -> {
                                RelaysScreen(
                                    strings = strings,
                                    nodes = nodes,
                                    activeNodeId = tunnelState.activeNodeId,
                                    selectedRegion = selectedRegion,
                                    searchQuery = searchQuery,
                                    isProbing = isProbing,
                                    onRegionSelected = viewModel::setRegionFilter,
                                    onSearchChanged = viewModel::setSearchQuery,
                                    onProbeAll = viewModel::probeAllNodeLatencies,
                                    onSelectNode = { node ->
                                        viewModel.selectNode(node, context)
                                        viewModel.selectTab(JissrTab.BRIDGE)
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onDeleteCustomNode = viewModel::deleteCustomNode,
                                    onAddCustomNode = viewModel::addCustomBridgeNode,
                                    modifier = contentModifier
                                )
                            }

                            JissrTab.SHIELD -> {
                                ShieldAndAppsScreen(
                                    strings = strings,
                                    tunnelState = tunnelState,
                                    onUpdateOption = { splitLan, dnsGuard, stealth, adBlock, killSwitch, ipv6Leak, mtu, proto ->
                                        viewModel.updateTunnelOption(
                                            splitTunnelBypassLan = splitLan,
                                            dnsLeakGuardEnabled = dnsGuard,
                                            stealthPaddingEnabled = stealth,
                                            adBlockerEnabled = adBlock,
                                            killSwitchEnabled = killSwitch,
                                            ipv6LeakProtectionEnabled = ipv6Leak,
                                            customMtu = mtu,
                                            customProtocol = proto
                                        )
                                    },
                                    onToggleAppBypass = viewModel::toggleAppBypass,
                                    modifier = contentModifier
                                )
                            }

                            JissrTab.DIAGNOSTICS -> {
                                DiagnosticsScreen(
                                    strings = strings,
                                    tunnelState = tunnelState,
                                    activeNode = activeNode,
                                    speedTestProgress = speedTestProgress,
                                    networkTrace = networkTrace,
                                    dnsBenchmarks = dnsBenchmarks,
                                    portProbeResult = portProbeResult,
                                    activeDnsServer = tunnelState.dnsServer,
                                    isRunningTrace = isRunningTrace,
                                    auditLogs = auditLogs,
                                    onRunTrace = viewModel::runFullNetworkDiagnostics,
                                    onProbeHostPort = viewModel::probeCustomHostPort,
                                    onApplyDnsServer = { dnsIp ->
                                        viewModel.updateTunnelOption(customDns = dnsIp)
                                    },
                                    onClearLogs = viewModel::clearAuditLogs,
                                    modifier = contentModifier
                                )
                            }

                            JissrTab.STUDIO -> {
                                val workflowYaml = remember(studioState) {
                                    viewModel.generateWorkflowYaml(studioState)
                                }
                                val jissrUri = remember(wgDraft, tunnelState, activeNode) {
                                    viewModel.generateJissrBridgeUri(tunnelState, activeNode)
                                }
                                StudioScreen(
                                    strings = strings,
                                    wgDraft = wgDraft,
                                    studioState = studioState,
                                    workflowYaml = workflowYaml,
                                    jissrShareUri = jissrUri,
                                    onUpdateWgDraft = viewModel::updateWgDraft,
                                    onGenerateKeypair = viewModel::generateNewWgKeypair,
                                    onGeneratePsk = viewModel::generateNewWgPsk,
                                    onImportConfText = viewModel::importWgConfText,
                                    onLoadActiveRelayPreset = {
                                        activeNode?.let { viewModel.loadWgDraftFromNode(it) }
                                    },
                                    onSaveDraftToBridgeNodes = {
                                        viewModel.saveWgDraftToBridgeNodes(context)
                                    },
                                    onUpdateStudio = viewModel::updateStudioConfig,
                                    modifier = contentModifier
                                )
                            }
                        }
                    }
                }
            }

            if (showQuickServerSheet) {
                QuickServerSwitchBottomSheet(
                    strings = strings,
                    tunnelState = tunnelState,
                    nodes = nodes,
                    onDismiss = { showQuickServerSheet = false },
                    onConnect = onConnectVpn,
                    onDisconnect = onDisconnectVpn,
                    onSmartFastestConnect = {
                        viewModel.selectFastestNodeAndConnect(context)
                    },
                    onSelectServerAndConnect = { node ->
                        viewModel.selectNode(node, context)
                        if (tunnelState.status != TunnelConnectionStatus.CONNECTED &&
                            tunnelState.status != TunnelConnectionStatus.CONNECTING
                        ) {
                            onConnectVpn()
                        }
                    },
                    onOpenAllRelaysTab = {
                        viewModel.selectTab(JissrTab.RELAYS)
                    }
                )
            }
        }
    }
}

@Composable
private fun JissrGlassNavigationDock(
    destinations: List<NavDestination>,
    currentTab: JissrTab,
    onSelectTab: (JissrTab) -> Unit
) {
    Surface(
        color = ObsidianSurface.copy(alpha = 0.96f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    JissrCyan.copy(alpha = 0.35f),
                    JissrEmerald.copy(alpha = 0.55f),
                    JissrCyan.copy(alpha = 0.35f)
                )
            )
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                val selected = currentTab == dest.tab
                val pillColor by animateColorAsState(
                    targetValue = if (selected) JissrEmerald.copy(alpha = 0.18f) else Color.Transparent,
                    label = "navPillColor"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (selected) JissrEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "navIconTint"
                )
                val topIndicatorWidth by animateDpAsState(
                    targetValue = if (selected) 24.dp else 0.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "topIndicatorWidth"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(dest.tab) }
                        .padding(vertical = 4.dp)
                        .testTag(dest.testTag)
                ) {
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .widthIn(min = topIndicatorWidth, max = topIndicatorWidth)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(JissrEmerald, JissrCyan)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pillColor,
                        border = if (selected) {
                            BorderStroke(1.dp, JissrEmerald.copy(alpha = 0.42f))
                        } else {
                            null
                        }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                tint = iconTint,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) JissrEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun JissrTopHeader(
    strings: JissrStrings,
    isConnected: Boolean,
    activeCity: String,
    activeFlag: String,
    peakDownloadMbps: Float,
    onOpenQuickServerSheet: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    Surface(
        color = ObsidianSurface.copy(alpha = 0.95f),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    if (isConnected) JissrEmerald.copy(alpha = 0.45f) else BorderSubtleDark,
                    JissrCyan.copy(alpha = 0.3f),
                    BorderSubtleDark
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (isConnected) JissrEmerald.copy(alpha = 0.16f) else ObsidianSurfaceVariant,
                    border = BorderStroke(
                        1.2.dp,
                        if (isConnected) JissrEmerald else JissrCyan.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_jissr_logo_crest),
                            contentDescription = strings.appTitle,
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
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = JissrEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, JissrEmerald.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = JissrEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f Mbps", peakDownloadMbps),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp
                                    ),
                                    color = JissrEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianCardElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) JissrEmerald.copy(alpha = 0.65f) else JissrCyan.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onOpenQuickServerSheet() }
                        .testTag("header_server_switch_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) JissrEmerald else JissrCyan)
                        )
                        Text(
                            text = "$activeFlag $activeCity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = JissrCyan.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Change Language",
                            tint = JissrCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (strings.isArabic) "EN" else "عربي",
                            style = MaterialTheme.typography.labelSmall,
                            color = JissrCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
