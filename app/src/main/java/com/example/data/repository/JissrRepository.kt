package com.example.data.repository

import com.example.data.local.BridgeNodeEntity
import com.example.data.local.JissrDao
import com.example.data.local.TunnelAuditLogEntity
import kotlinx.coroutines.flow.Flow

class JissrRepository(private val dao: JissrDao) {

    val allNodes: Flow<List<BridgeNodeEntity>> = dao.observeAllNodes()
    val auditLogs: Flow<List<TunnelAuditLogEntity>> = dao.observeAuditLogs()

    suspend fun ensureSeeded(): List<BridgeNodeEntity> {
        val current = dao.getAllNodesOnce()
        if (current.isNotEmpty()) return current

        val defaultNodes = listOf(
            BridgeNodeEntity(
                id = "dxb-jissr-01",
                city = "Dubai",
                cityAr = "دبي",
                country = "United Arab Emirates",
                countryAr = "الإمارات العربية المتحدة",
                region = "Middle East",
                flagEmoji = "🇦🇪",
                endpointHost = "1.1.1.1",
                endpointPort = 443,
                protocol = "WireGuard-Jissr",
                obfuscationMode = "Stealth TLS 1.3",
                virtualIp = "10.88.11.2",
                dnsPrimary = "1.1.1.1",
                mtu = 1420,
                publicKey = "JissrDXB9xK4mP8qR2vL7nZ1wC5tY3bF6hJ0sA4dG8k=",
                serverLoadPercent = 22,
                isFavorite = true
            ),
            BridgeNodeEntity(
                id = "ruh-jissr-02",
                city = "Riyadh",
                cityAr = "الرياض",
                country = "Saudi Arabia",
                countryAr = "المملكة العربية السعودية",
                region = "Middle East",
                flagEmoji = "🇸🇦",
                endpointHost = "1.0.0.1",
                endpointPort = 443,
                protocol = "VLESS-Reality",
                obfuscationMode = "Fragmented SNI",
                virtualIp = "10.88.12.2",
                dnsPrimary = "1.0.0.1",
                mtu = 1380,
                publicKey = "JissrRUH4pL9vM2xQ7kT1wN6zB8cR5yF3hD0jG4sA9e=",
                serverLoadPercent = 19,
                isFavorite = true
            ),
            BridgeNodeEntity(
                id = "doh-jissr-03",
                city = "Doha",
                cityAr = "الدوحة",
                country = "Qatar",
                countryAr = "قطر",
                region = "Middle East",
                flagEmoji = "🇶🇦",
                endpointHost = "1.1.1.1",
                endpointPort = 443,
                protocol = "Hysteria2-QUIC",
                obfuscationMode = "QUIC Multi-Hop",
                virtualIp = "10.88.13.2",
                dnsPrimary = "1.1.1.1",
                mtu = 1400,
                publicKey = "JissrDOH7nK2vM9pL4xQ8rZ1wT5yC3bF6hJ0sA4dG2k=",
                serverLoadPercent = 16,
                isFavorite = true
            ),
            BridgeNodeEntity(
                id = "amm-jissr-04",
                city = "Amman",
                cityAr = "عمّان",
                country = "Jordan",
                countryAr = "الأردن",
                region = "Middle East",
                flagEmoji = "🇯🇴",
                endpointHost = "8.8.8.8",
                endpointPort = 443,
                protocol = "WireGuard-Jissr",
                obfuscationMode = "Stealth TLS 1.3",
                virtualIp = "10.88.14.2",
                dnsPrimary = "8.8.8.8",
                mtu = 1420,
                publicKey = "JissrAMM3xP8vL2kM9qR4nZ7wT1yC5bF6hJ0sD8gA3e=",
                serverLoadPercent = 27
            ),
            BridgeNodeEntity(
                id = "ist-jissr-05",
                city = "Istanbul",
                cityAr = "إسطنبول",
                country = "Turkey",
                countryAr = "تركيا",
                region = "Middle East",
                flagEmoji = "🇹🇷",
                endpointHost = "8.8.4.4",
                endpointPort = 443,
                protocol = "VLESS-Reality",
                obfuscationMode = "Fragmented SNI",
                virtualIp = "10.88.15.2",
                dnsPrimary = "1.1.1.1",
                mtu = 1380,
                publicKey = "JissrIST5mK9vL3pQ7xR2nZ8wT4yC1bF6hJ0sA2dG5k=",
                serverLoadPercent = 34
            ),
            BridgeNodeEntity(
                id = "fra-jissr-06",
                city = "Frankfurt",
                cityAr = "فرانكفورت",
                country = "Germany",
                countryAr = "ألمانيا",
                region = "Europe",
                flagEmoji = "🇩🇪",
                endpointHost = "8.8.8.8",
                endpointPort = 443,
                protocol = "WireGuard-Jissr",
                obfuscationMode = "QUIC Multi-Hop",
                virtualIp = "10.88.21.2",
                dnsPrimary = "8.8.8.8",
                mtu = 1420,
                publicKey = "JissrFRA7mQ2kX9pL4vR8nZ3wT6yC1bF5hJ0sD4gA7k=",
                serverLoadPercent = 24,
                isFavorite = true
            ),
            BridgeNodeEntity(
                id = "ams-jissr-07",
                city = "Amsterdam",
                cityAr = "أمستردام",
                country = "Netherlands",
                countryAr = "هولندا",
                region = "Europe",
                flagEmoji = "🇳🇱",
                endpointHost = "9.9.9.9",
                endpointPort = 443,
                protocol = "WireGuard-Jissr",
                obfuscationMode = "Direct UDP",
                virtualIp = "10.88.22.2",
                dnsPrimary = "9.9.9.9",
                mtu = 1420,
                publicKey = "JissrAMS2vL8nK4pQ9xR1mZ7wT5cY3bF6hJ0sG4dA8e=",
                serverLoadPercent = 31
            ),
            BridgeNodeEntity(
                id = "zrh-jissr-08",
                city = "Zurich",
                cityAr = "زيورخ",
                country = "Switzerland",
                countryAr = "سويسرا",
                region = "Europe",
                flagEmoji = "🇨🇭",
                endpointHost = "149.112.112.112",
                endpointPort = 443,
                protocol = "VLESS-Reality",
                obfuscationMode = "Stealth TLS 1.3",
                virtualIp = "10.88.23.2",
                dnsPrimary = "9.9.9.9",
                mtu = 1400,
                publicKey = "JissrZRH6kP1mX8vL3qR9nZ2wT7yC4bF5hJ0sA8dG1k=",
                serverLoadPercent = 18
            ),
            BridgeNodeEntity(
                id = "lon-jissr-09",
                city = "London",
                cityAr = "لندن",
                country = "United Kingdom",
                countryAr = "المملكة المتحدة",
                region = "Europe",
                flagEmoji = "🇬🇧",
                endpointHost = "8.8.4.4",
                endpointPort = 443,
                protocol = "Shadowsocks-2022",
                obfuscationMode = "Stealth TLS 1.3",
                virtualIp = "10.88.24.2",
                dnsPrimary = "1.1.1.1",
                mtu = 1420,
                publicKey = "JissrLON8xR3pK9vM2qL7nZ1wT4yC6bF5hJ0sD2gA9e=",
                serverLoadPercent = 29
            ),
            BridgeNodeEntity(
                id = "sin-jissr-10",
                city = "Singapore",
                cityAr = "سنغافورة",
                country = "Singapore",
                countryAr = "سنغافورة",
                region = "Asia",
                flagEmoji = "🇸🇬",
                endpointHost = "208.67.222.222",
                endpointPort = 443,
                protocol = "Hysteria2-QUIC",
                obfuscationMode = "QUIC Multi-Hop",
                virtualIp = "10.88.31.2",
                dnsPrimary = "208.67.222.222",
                mtu = 1420,
                publicKey = "JissrSIN5nZ9wT2kP7xL4vR8mQ1yC3bF6hJ0sA4dG7k=",
                serverLoadPercent = 25
            ),
            BridgeNodeEntity(
                id = "tyo-jissr-11",
                city = "Tokyo",
                cityAr = "طوكيو",
                country = "Japan",
                countryAr = "اليابان",
                region = "Asia",
                flagEmoji = "🇯🇵",
                endpointHost = "208.67.220.220",
                endpointPort = 443,
                protocol = "VLESS-Reality",
                obfuscationMode = "Fragmented SNI",
                virtualIp = "10.88.32.2",
                dnsPrimary = "1.1.1.1",
                mtu = 1380,
                publicKey = "JissrTYO3qL7vM9xK2pR8nZ4wT1yC5bF6hJ0sD8gA2e=",
                serverLoadPercent = 33
            ),
            BridgeNodeEntity(
                id = "nyc-jissr-12",
                city = "New York",
                cityAr = "نيويورك",
                country = "United States",
                countryAr = "الولايات المتحدة",
                region = "Americas",
                flagEmoji = "🇺🇸",
                endpointHost = "94.140.14.14",
                endpointPort = 443,
                protocol = "WireGuard-Jissr",
                obfuscationMode = "Direct UDP",
                virtualIp = "10.88.41.2",
                dnsPrimary = "94.140.14.14",
                mtu = 1420,
                publicKey = "JissrNYC9mK4vL2pQ8xR7nZ1wT3yC6bF5hJ0sA2dG9k=",
                serverLoadPercent = 28
            )
        )
        dao.insertNodes(defaultNodes)
        dao.insertAuditLog(
            TunnelAuditLogEntity(
                nodeName = "نواة جسر الأمنية (Jissr Core)",
                protocol = "WireGuard-Jissr",
                eventTitle = "تم تهيئة 12 خادم جسر عالمي مشفر",
                details = "تم تفعيل درع DNS ومقاومة الفحص العميق للحزم DPI وتقسيم التطبيقات."
            )
        )
        return defaultNodes
    }

    suspend fun getAllNodesOnce(): List<BridgeNodeEntity> = dao.getAllNodesOnce()

    suspend fun updateNodeLatency(id: String, latencyMs: Int) {
        dao.updateNodeLatency(id, latencyMs)
    }

    suspend fun toggleFavorite(node: BridgeNodeEntity) {
        dao.updateNodeFavorite(node.id, !node.isFavorite)
    }

    suspend fun addCustomNode(node: BridgeNodeEntity) {
        dao.insertNode(node)
        dao.insertAuditLog(
            TunnelAuditLogEntity(
                nodeName = "${node.cityAr} (${node.endpointHost}:${node.endpointPort})",
                protocol = node.protocol,
                eventTitle = "تمت إضافة خادم جسر مخصص",
                details = "البروتوكول ${node.protocol} · التمويه ${node.obfuscationMode} · MTU ${node.mtu}."
            )
        )
    }

    suspend fun deleteCustomNode(node: BridgeNodeEntity) {
        if (node.isCustom) {
            dao.deleteCustomNode(node.id)
            dao.insertAuditLog(
                TunnelAuditLogEntity(
                    nodeName = node.cityAr,
                    protocol = node.protocol,
                    eventTitle = "تم حذف الخادم المخصص",
                    details = "تمت إزالة ${node.endpointHost}:${node.endpointPort}."
                )
            )
        }
    }

    suspend fun recordLog(nodeName: String, protocol: String, title: String, details: String) {
        dao.insertAuditLog(
            TunnelAuditLogEntity(
                nodeName = nodeName,
                protocol = protocol,
                eventTitle = title,
                details = details
            )
        )
    }

    suspend fun clearLogs() {
        dao.clearAuditLogs()
    }
}
