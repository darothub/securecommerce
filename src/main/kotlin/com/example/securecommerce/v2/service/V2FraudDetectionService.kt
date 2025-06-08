package com.example.securecommerce.v2.service

import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.v1.service.FraudResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class V2FraudDetectionService {
    private val fraudScoreCache = ConcurrentHashMap<String, FraudResult>()
    @Cacheable(value = ["V2FraudScores"], key = "#request.cardNumber.substring(#request.cardNumber.length() - 4) + '_' + #request.merchantId")
    suspend fun checkFraudAsync(request: PaymentRequest): FraudResult {
        val cacheKey = "${request.cardNumber.takeLast(4)}_${request.merchantId}_${request.amount}"
        return fraudScoreCache.computeIfAbsent(cacheKey) {
            runBlocking {
                delay(300) // Reduced from 1000ms to 300ms

                val isHighRisk = request.amount.toDouble() > 10000.0 ||
                        request.cardNumber.startsWith("4000000000000119")

                FraudResult(
                    isHighRisk = isHighRisk,
                    riskScore = if (isHighRisk) 85.0 else 15.0,
                    reasons = if (isHighRisk) listOf("High amount", "Suspicious card pattern") else emptyList()
                )
            }
        }
    }
}