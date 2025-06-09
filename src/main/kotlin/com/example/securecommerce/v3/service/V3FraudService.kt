package com.example.securecommerce.v3.service

import com.example.securecommerce.v3.dto.V3SecurePaymentRequest
import org.springframework.stereotype.Service

@Service
class V3FraudService {
    fun checkFraudSecure(request: V3SecurePaymentRequest): V3FraudResult {
        // ⚠️ PERFORMANCE TRADE-OFF: More thorough but slower fraud check
        Thread.sleep(800) // Slower for comprehensive security

        val riskFactors = mutableListOf<String>()
        var riskScore = 10.0

        if (request.amount.toDouble() > 10000.0) {
            riskFactors.add("High amount")
            riskScore += 60.0
        }

        if (request.cardholderName.length < 3) {
            riskFactors.add("Suspicious name")
            riskScore += 25.0
        }

        val isHighRisk = riskScore > 50.0

        return V3FraudResult(isHighRisk, riskScore, riskFactors)
    }
}
@Service
class V3GatewayService {
    fun processPaymentSecure(cardNumber: String, transactionId: String): V3GatewayResult {
        // ⚠️ PERFORMANCE TRADE-OFF: More secure but slower gateway processing
        Thread.sleep(1200) // Slower for enhanced security validation

        val success = cardNumber != "4000000000000002"
        return V3GatewayResult(success, if (!success) "Card declined by secure gateway" else null)
    }
}
data class V3FraudResult(val isHighRisk: Boolean, val riskScore: Double, val riskFactors: List<String>)
data class V3GatewayResult(val success: Boolean, val error: String?)