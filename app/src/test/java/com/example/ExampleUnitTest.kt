package com.example

import com.example.ui.WireGuardProfileDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun wireGuardConf_roundTripParseAndSerialize() {
        val sampleConf = """
            # Name = Riyadh-Fast-WG
            [Interface]
            PrivateKey = oG8xK4mP9qR2vL7nZ1wC5tY3bF6hJ0sA4dG8kM2pQ1E=
            Address = 10.88.12.2/24
            DNS = 1.1.1.1, 1.0.0.1
            MTU = 1380
            ListenPort = 51820

            [Peer]
            PublicKey = JissrRUH4pL9vM2xQ7kT1wN6zB8cR5yF3hD0jG4sA9e=
            PresharedKey = psk9xK4mP8qR2vL7nZ1wC5tY3bF6hJ0sA4dG8kM2pQ1=
            Endpoint = 1.0.0.1:443
            AllowedIPs = 0.0.0.0/0, ::/0
            PersistentKeepalive = 25
        """.trimIndent()

        val parsed = WireGuardProfileDraft.parseFromConf(sampleConf)
        assertEquals("Riyadh-Fast-WG", parsed.profileName)
        assertEquals("10.88.12.2/24", parsed.interfaceAddress)
        assertEquals("1380", parsed.interfaceMtu)
        assertEquals("1.0.0.1:443", parsed.peerEndpoint)
        assertTrue(parsed.validate(isArabic = false).isEmpty())

        val exported = parsed.toConfString()
        assertTrue(exported.contains("[Interface]"))
        assertTrue(exported.contains("Endpoint = 1.0.0.1:443"))
    }
}
