package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

data class AiTunnelOptimizationPlan(
    val headline: String,
    val recommendedServerCity: String,
    val recommendedProtocol: String,
    val recommendedMtu: Int,
    val recommendedDns: String,
    val estimatedSpeedGainPercent: Int,
    val optimizationSteps: List<String>
)

sealed interface AiOptimizationUiState {
    data object Idle : AiOptimizationUiState
    data object Loading : AiOptimizationUiState
    data class Streaming(val partialText: String, val plan: AiTunnelOptimizationPlan? = null) : AiOptimizationUiState
    data class Success(
        val plan: AiTunnelOptimizationPlan,
        val advisoryText: String = "",
        val applied: Boolean = false
    ) : AiOptimizationUiState
    data class Error(val message: String) : AiOptimizationUiState
}

enum class AiOptimizationGoal(
    val labelAr: String,
    val labelEn: String,
    val promptFocusAr: String,
    val promptFocusEn: String
) {
    MAX_SPEED(
        labelAr = "أقصى سرعة تحميل",
        labelEn = "Max Speed",
        promptFocusAr = "تحقيق أعلى سرعة تنزيل ورفع وتقليل فقد الحزم عبر ضبط حجم MTU وخادم الترحيل الأسرع.",
        promptFocusEn = "Maximize download/upload throughput and eliminate packet fragmentation via optimal MTU and fastest relay."
    ),
    ULTRA_LOW_PING(
        labelAr = "أقل بنق للألعاب",
        labelEn = "Gaming Low Ping",
        promptFocusAr = "تقليل زمن الاستجابة (Ping) والتذبذب (Jitter) للألعاب التنافسية عبر أقرب خادم جغرافي وبروتوكول UDP مباشر.",
        promptFocusEn = "Minimize latency (Ping) and jitter for competitive gaming using the lowest-RTT relay and direct UDP framing."
    ),
    DEEP_STEALTH(
        labelAr = "تخفي وتجاوز الحجب",
        labelEn = "Deep Stealth DPI",
        promptFocusAr = "تفعيل التمويه العميق ضد فحص الحزم (DPI) وتشفير DNS عبر HTTPS مع MTU متوافق مع الشبكات المقيدة.",
        promptFocusEn = "Enable deep DPI obfuscation, TLS padding, and encrypted DNS with a conservative MTU for restricted networks."
    ),
    STREAMING_4K(
        labelAr = "بث 4K بدون تقطيع",
        labelEn = "4K Streaming",
        promptFocusAr = "استقرار تدفق الفيديو بدقة 4K وتجنب اختناق المزود (ISP Throttling) مع DNS سريع للاستجابة الفورية.",
        promptFocusEn = "Stabilize high-bitrate 4K video buffers and bypass ISP throttling with low-latency CDN DNS resolution."
    )
}

class JissrAiOptimizer {

    private val optimizationSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(
                description = "A concise, professional summary of the AI tunnel optimization analysis"
            ),
            "recommendedServerCity" to Schema.string(
                description = "The city name of the recommended relay server from the provided node list"
            ),
            "recommendedProtocol" to Schema.string(
                description = "Recommended VPN protocol such as WireGuard-UDP, WireGuard-Stealth, or VLESS-Reality"
            ),
            "recommendedMtu" to Schema.integer(
                description = "Recommended MTU size between 1280 and 1480"
            ),
            "recommendedDns" to Schema.string(
                description = "Recommended primary DNS IPv4 address such as 1.1.1.1, 9.9.9.9, 8.8.8.8, or 94.140.14.14"
            ),
            "estimatedSpeedGainPercent" to Schema.integer(
                description = "Estimated percentage improvement in throughput or latency stability (between 8 and 45)"
            ),
            "optimizationSteps" to Schema.array(
                items = Schema.string(),
                description = "Three specific technical optimization steps tailored to current telemetry"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = optimizationSchema
            }
        )
    }

    private val streamingAdvisorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun generateOptimizationPlan(prompt: String): AiTunnelOptimizationPlan {
        val response = structuredModel.generateContent(prompt)
        val rawJson = response.text ?: throw IllegalStateException("Model returned an empty response.")
        val json = JSONObject(rawJson)

        val stepsArray = json.optJSONArray("optimizationSteps")
        val steps = buildList {
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    val item = stepsArray.optString(i).trim()
                    if (item.isNotEmpty()) add(item)
                }
            }
        }

        return AiTunnelOptimizationPlan(
            headline = json.optString("headline", ""),
            recommendedServerCity = json.optString("recommendedServerCity", "Frankfurt"),
            recommendedProtocol = json.optString("recommendedProtocol", "WireGuard-UDP"),
            recommendedMtu = json.optInt("recommendedMtu", 1420).coerceIn(1280, 1480),
            recommendedDns = json.optString("recommendedDns", "1.1.1.1"),
            estimatedSpeedGainPercent = json.optInt("estimatedSpeedGainPercent", 22).coerceIn(5, 65),
            optimizationSteps = steps
        )
    }

    fun streamCustomAdvisory(prompt: String): Flow<String> {
        return streamingAdvisorModel.generateContentStream(prompt).map { chunk ->
            chunk.text.orEmpty()
        }
    }
}
