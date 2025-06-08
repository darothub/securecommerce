package com.example.securecommerce.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Schema(description = "Payment processing request")
data class PaymentRequest(
    @field:NotBlank(message = "Merchant ID is required")
    @Schema(description = "Merchant identifier", example = "MERCHANT_123")
    val merchantId: String,
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Payment amount", example = "99.99", required = true)
    val amount: BigDecimal,
    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters long")
    @Schema(description = "Currency code", example = "EUR")
    val currency: String,
    @field:NotBlank(message = "Card number is required")
    @field:Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number format")
    @Schema(description = "Credit card number", example = "4111111111111111")
    val cardNumber: String,
    @field:NotBlank(message = "Cardholder name is required")
    @Schema(description = "Name on card", example = "John Doe")
    val cardholderName: String,
    @field:NotNull(message = "Expiry month is required")
    @field:Min(value = 1, message = "Expiry month must be between 1 and 12")
    @field:Max(value = 12, message = "Expiry month must be between 1 and 12")
    @Schema(description = "Card expiry month", example = "12")
    val expiryMonth: Int,
    @field:NotNull(message = "Expiry year is required")
    @field:Min(value = 2024, message = "Expiry year must be in the future")
    @Schema(description = "Card expiry year", example = "2025")
    val expiryYear: Int,
    @field:NotBlank(message = "CVV is required")
    @field:Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @Schema(description = "Card CVV", example = "123")
    val cvv: String,
)

