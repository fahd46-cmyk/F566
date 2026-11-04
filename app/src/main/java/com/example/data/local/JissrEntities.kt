package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bridge_nodes")
data class BridgeNodeEntity(
    @PrimaryKey val id: String,
    val city: String,
    val cityAr: String = city,
    val country: String,
    val countryAr: String = country,
    val region: String, // "Middle East", "Europe", "Asia", "Americas", "Custom"
    val flagEmoji: String,
    val endpointHost: String,
    val endpointPort: Int,
    val protocol: String, // "WireGuard-Jissr", "VLESS-Reality", "Hysteria2-QUIC", "Shadowsocks-2022"
    val obfuscationMode: String, // "Stealth TLS 1.3", "QUIC Multi-Hop", "Fragmented SNI", "Direct UDP"
    val virtualIp: String,
    val dnsPrimary: String,
    val mtu: Int,
    val publicKey: String,
    val serverLoadPercent: Int = 28,
    val lastLatencyMs: Int = -1, // -1 = unprobed, >0 = real measured TCP handshake RTT in ms
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) {
    fun localizedCity(isArabic: Boolean): String = if (isArabic && cityAr.isNotBlank()) cityAr else city
    fun localizedCountry(isArabic: Boolean): String = if (isArabic && countryAr.isNotBlank()) countryAr else country
}

@Entity(tableName = "tunnel_audit_logs")
data class TunnelAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val nodeName: String,
    val protocol: String,
    val eventTitle: String,
    val details: String
)
