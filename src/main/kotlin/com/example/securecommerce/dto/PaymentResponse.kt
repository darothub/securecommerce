package com.example.securecommerce.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Payment processing response")
data class PaymentResponse(
    @Schema(description = "Transaction ID", example = "TXN_123456789")
    val transactionId: String,

    @Schema(description = "Payment status", example = "PENDING")
    val status: String,

    @Schema(description = "Processing message", example = "Payment submitted successfully")
    val message: String,

    @Schema(description = "Amount processed", example = "99.99")
    val amount: BigDecimal,

    @Schema(description = "Currency", example = "USD")
    val currency: String,

    // v2+: Performance metrics
    @Schema(description = "Processing time in milliseconds")
    val processingTimeMs: Long? = null,

    // v3+: Security features
    @Schema(description = "Masked card number for display")
    val maskedCardNumber: String? = null,

    @Schema(description = "Card token for future use")
    val cardToken: String? = null,

    @Schema(description = "Risk assessment score")
    val riskScore: Double? = null
)
