package com.example.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TunnelConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class SmartBridgeMode {
    STEALTH,
    GAMING,
    STREAMING,
    DOUBLE_HOP
}

data class VpnTunnelState(
    val status: TunnelConnectionStatus = TunnelConnectionStatus.DISCONNECTED,
    val smartMode: SmartBridgeMode = SmartBridgeMode.STEALTH,
    val activeNodeId: String = "dxb-jissr-01",
    val activeCity: String = "Dubai",
    val activeCityAr: String = "دبي",
    val activeCountry: String = "United Arab Emirates",
    val activeCountryAr: String = "الإمارات العربية المتحدة",
    val activeFlag: String = "🇦🇪",
    val activeProtocol: String = "WireGuard-Jissr",
    val activeObfuscation: String = "Stealth TLS 1.3",
    val virtualIp: String = "10.88.11.2",
    val dnsServer: String = "1.1.1.1",
    val mtu: Int = 1420,
    val isNativeTunActive: Boolean = false,
    val uptimeSeconds: Long = 0L,
    val downloadSpeedBps: Long = 0L,
    val uploadSpeedBps: Long = 0L,
    val totalSessionRxBytes: Long = 0L,
    val totalSessionTxBytes: Long = 0L,
    val tunPacketsInspected: Long = 0L,
    val blockedTrackersCount: Long = 14L,
    val throughputHistoryKbps: List<Float> = List(24) { 0f },
    val splitTunnelBypassLan: Boolean = true,
    val dnsLeakGuardEnabled: Boolean = true,
    val stealthPaddingEnabled: Boolean = true,
    val adBlockerEnabled: Boolean = true,
    val killSwitchEnabled: Boolean = true,
    val ipv6LeakProtectionEnabled: Boolean = true,
    val bypassedPackages: Set<String> = emptySet()
) {
    fun displayCity(isArabic: Boolean): String = if (isArabic && activeCityAr.isNotBlank()) activeCityAr else activeCity
    fun displayCountry(isArabic: Boolean): String = if (isArabic && activeCountryAr.isNotBlank()) activeCountryAr else activeCountry

    val securityScorePercent: Int
        get() {
            var score = 62
            if (dnsLeakGuardEnabled) score += 10
            if (stealthPaddingEnabled) score += 8
            if (adBlockerEnabled) score += 8
            if (killSwitchEnabled) score += 6
            if (ipv6LeakProtectionEnabled) score += 6
            return score.coerceIn(50, 100)
        }
}

object VpnTunnelController {
    private val _state = MutableStateFlow(VpnTunnelState())
    val state: StateFlow<VpnTunnelState> = _state.asStateFlow()

    fun updateState(transform: (VpnTunnelState) -> VpnTunnelState) {
        _state.update(transform)
    }

    fun setOptions(
        splitTunnelBypassLan: Boolean? = null,
        dnsLeakGuardEnabled: Boolean? = null,
        stealthPaddingEnabled: Boolean? = null,
        adBlockerEnabled: Boolean? = null,
        killSwitchEnabled: Boolean? = null,
        ipv6LeakProtectionEnabled: Boolean? = null,
        customDns: String? = null,
        customMtu: Int? = null,
        customProtocol: String? = null,
        smartMode: SmartBridgeMode? = null
    ) {
        _state.update { current ->
            current.copy(
                splitTunnelBypassLan = splitTunnelBypassLan ?: current.splitTunnelBypassLan,
                dnsLeakGuardEnabled = dnsLeakGuardEnabled ?: current.dnsLeakGuardEnabled,
                stealthPaddingEnabled = stealthPaddingEnabled ?: current.stealthPaddingEnabled,
                adBlockerEnabled = adBlockerEnabled ?: current.adBlockerEnabled,
                killSwitchEnabled = killSwitchEnabled ?: current.killSwitchEnabled,
                ipv6LeakProtectionEnabled = ipv6LeakProtectionEnabled ?: current.ipv6LeakProtectionEnabled,
                dnsServer = customDns ?: current.dnsServer,
                mtu = customMtu ?: current.mtu,
                activeProtocol = customProtocol ?: current.activeProtocol,
                smartMode = smartMode ?: current.smartMode
            )
        }
    }

    fun toggleAppBypass(packageName: String) {
        _state.update { current ->
            val updated = current.bypassedPackages.toMutableSet()
            if (updated.contains(packageName)) {
                updated.remove(packageName)
            } else {
                updated.add(packageName)
            }
            current.copy(bypassedPackages = updated)
        }
    }
}
