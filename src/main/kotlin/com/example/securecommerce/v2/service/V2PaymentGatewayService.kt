package com.example.securecommerce.v2.service

import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.v1.service.GatewayResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class V2PaymentGatewayService {

    private val cardValidationCache = ConcurrentHashMap<String, CardValidationResult>()
    suspend fun validateCardAsync(request: PaymentRequest): CardValidationResult {
        // PERFORMANCE IMPROVEMENT: Caching + reduced time
        val cacheKey = "${request.cardNumber.takeLast(4)}_${request.expiryMonth}_${request.expiryYear}"

        return cardValidationCache.computeIfAbsent(cacheKey){
            runBlocking {
                delay(200)
                CardValidationResult(
                    isValid = request.cardNumber != "4000000000000002",
                    errorMessage = if (request.cardNumber == "4000000000000002") "Invalid card number" else null
                )
            }
        }
    }

    suspend fun processPaymentAsync(request: PaymentRequest, transactionId: String): GatewayResult {
        delay(800)
        val isSuccess = request.cardNumber != "4000000000000002"
        return GatewayResult(
            isSuccess = isSuccess,
            gatewayTransactionId = "GW_${transactionId}",
            errorMessage = if (!isSuccess) "Card declined by issuer" else null
        )
    }
}

data class CardValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)