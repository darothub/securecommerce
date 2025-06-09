package com.example.securecommerce.v3.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Schema(description = "V3 Secure payment request with PCI DSS compliance")
data class V3SecurePaymentRequest(
    @field:NotBlank(message = "Merchant ID is required")
    @Schema(description = "Merchant identifier", example = "MERCHANT_123")
    val merchantId: String,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @field:DecimalMax(value = "999999.99", message = "Amount exceeds maximum limit")
    @Schema(description = "Payment amount", example = "99.99")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase ISO code")
    @Schema(description = "Currency code", example = "USD")
    val currency: String,

    @field:NotBlank(message = "Card number is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Credit card number (will be tokenized)", example = "4111111111111111")
    val cardNumber: String,

    @field:NotBlank(message = "Cardholder name is required")
    @field:Size(max = 100, message = "Cardholder name too long")
    @Schema(description = "Name on card", example = "John Doe")
    val cardholderName: String,

    @field:NotNull(message = "Expiry month is required")
    @field:Min(value = 1) @field:Max(value = 12)
    @Schema(description = "Card expiry month", example = "12")
    val expiryMonth: Int,

    @field:NotNull(message = "Expiry year is required")
    @field:Min(value = 2024) @field:Max(value = 2040)
    @Schema(description = "Card expiry year", example = "2025")
    val expiryYear: Int,

    @field:NotBlank(message = "CVV is required")
    @field:Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val cvcv: String,
)

@Schema(description = "V3 Secure payment response")
data class V3SecurePaymentResponse(
    val transactionId: String,
    val status: String,
    val message: String,
    val amount: BigDecimal,
    val currency: String,
    val cardToken: String,
    val maskedCardNumber: String,
    val cardType: String,
    val riskScore: Double,
    val processingTimeMs: Long,
    val securityCompliance: String = "PCI_DSS_COMPLIANT"
)