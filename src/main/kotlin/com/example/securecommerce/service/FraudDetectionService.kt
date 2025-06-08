package com.example.securecommerce.service

import com.example.securecommerce.dto.PaymentRequest
import org.springframework.stereotype.Service

@Service
class FraudDetectionService {

    fun checkFraud(request: PaymentRequest): FraudResult {
        // PERFORMANCE ISSUE: Simulate slow ML fraud detection
        Thread.sleep(1000)

        // Simple fraud rules for demo
        val isHighRisk = request.amount.toDouble() > 10000.0 ||
                request.cardNumber.startsWith("4000000000000119")

        return FraudResult(
            isHighRisk = isHighRisk,
            riskScore = if (isHighRisk) 85.0 else 15.0,
            reasons = if (isHighRisk) listOf("High amount", "Suspicious card pattern") else emptyList()
        )
    }
}

data class FraudResult(
    val isHighRisk: Boolean,
    val riskScore: Double,
    val reasons: List<String>
)