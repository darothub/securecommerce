package com.example.securecommerce.v1.service

import com.example.securecommerce.dto.PaymentRequest
import org.springframework.stereotype.Service

@Service
class V1PaymentGatewayService {

    fun processPayment(request: PaymentRequest, transactionId: String): GatewayResult {
        // PERFORMANCE ISSUE: Simulate slow external API call
        Thread.sleep(1500)

        // Simple simulation - approve most payments
        val isSuccess = request.cardNumber != "4000000000000002" // Decline this test card

        return GatewayResult(
            isSuccess = isSuccess,
            gatewayTransactionId = "GW_${transactionId}",
            errorMessage = if (!isSuccess) "Card declined by issuer" else null
        )
    }
}

data class GatewayResult(
    val isSuccess: Boolean,
    val gatewayTransactionId: String,
    val errorMessage: String? = null
)