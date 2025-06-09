package com.example.securecommerce.v3.security

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SecurityAuditLogger {
    private val auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT")

    fun logCardTokenization(token: String, maskedCard: String, merchantId: String) {
        logSecurityEvent("CARD_TOKENIZATION", mapOf(
            "token" to token,
            "maskedCard" to maskedCard,
            "merchantId" to merchantId,
            "action" to "CREATE_TOKEN"
        ))
    }

    fun logCardDetokenization(token: String, merchantId: String) {
        logSecurityEvent("CARD_DETOKENIZATION", mapOf(
            "token" to token,
            "merchantId" to merchantId,
            "action" to "ACCESS_CARD_DATA"
        ))
    }

    fun logInvalidTokenAccess(token: String, merchantId: String) {
        logSecurityEvent("INVALID_TOKEN_ACCESS", mapOf(
            "token" to token,
            "merchantId" to merchantId,
            "action" to "SECURITY_VIOLATION",
            "severity" to "HIGH"
        ))
    }

    fun logAuthenticationAttempt(merchantId: String, success: Boolean, ip: String?) {
        logSecurityEvent("AUTHENTICATION_ATTEMPT", mapOf(
            "merchantId" to merchantId,
            "success" to success.toString(),
            "ipAddress" to (ip ?: "unknown"),
            "action" to if (success) "LOGIN_SUCCESS" else "LOGIN_FAILED"
        ))
    }

    fun logSecurityEvent(eventType: String, details: Map<String, String>) {
        try {
            MDC.put("eventType", eventType)
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

            details.forEach { (key, value) ->
                MDC.put(key, value)
            }

            auditLogger.info("Security event: {} - {}", eventType, details)

        } finally {
            MDC.clear()
        }
    }
}