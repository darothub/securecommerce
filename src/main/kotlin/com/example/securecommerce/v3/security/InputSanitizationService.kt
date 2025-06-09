package com.example.securecommerce.v3.security

import org.apache.commons.lang3.StringEscapeUtils
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class InputSanitizationService {
    private val cardNumberPattern = Pattern.compile("^[0-9]{13,19}$")
    private val alphanumericPattern = Pattern.compile("^[a-zA-Z0-9\\s\\-\\.]+$")
    private val merchantIdPattern = Pattern.compile("^[A-Z0-9_]{3,50}$")

    fun sanitizeCardNumber(cardNumber: String?): String {
        if (cardNumber.isNullOrBlank()) {
            throw IllegalArgumentException("Card number cannot be empty")
        }

        val cleaned = cardNumber.replace(Regex("[^0-9]"), "")

        if (!cardNumberPattern.matcher(cleaned).matches()) {
            throw IllegalArgumentException("Invalid card number format")
        }

        return cleaned
    }

    fun sanitizeCardholderName(name: String?): String {
        if (name.isNullOrBlank()) {
            throw IllegalArgumentException("Cardholder name cannot be empty")
        }

        val sanitized = name.trim()
        val escaped = StringEscapeUtils.escapeHtml4(sanitized)

        if (escaped.length > 100 || !alphanumericPattern.matcher(escaped).matches()) {
            throw IllegalArgumentException("Invalid cardholder name format")
        }

        return escaped
    }
    fun sanitizeMerchantId(merchantId: String?): String {
        if (merchantId.isNullOrBlank()) {
            throw IllegalArgumentException("Merchant ID cannot be empty")
        }

        val sanitized = merchantId.trim().uppercase()

        if (!merchantIdPattern.matcher(sanitized).matches()) {
            throw IllegalArgumentException("Invalid merchant ID format")
        }

        return sanitized
    }
}