package com.example.securecommerce.v3.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class CardTokenizationService(
    @Value("\${app.security.encryption.key:#{null}}") private val encryptionKeyBase64: String?,
    private val auditLogger: SecurityAuditLogger
) {

    private val logger = LoggerFactory.getLogger(CardTokenizationService::class.java)
    private val secureRandom = SecureRandom()
    private val tokenVault = ConcurrentHashMap<String, String>() // In production: use HSM
    private val encryptionKey: SecretKey by lazy {
        if (!encryptionKeyBase64.isNullOrBlank()) {
            val keyBytes = Base64.getDecoder().decode(encryptionKeyBase64)
            SecretKeySpec(keyBytes, "AES")
        } else {
            generateEncryptionKey()
        }
    }

    fun tokenizeCard(cardNumber: String, merchantId: String): CardToken {
        validateCardNumber(cardNumber)

        val cleanCardNumber = cardNumber.replace(Regex("[^0-9]"), "")
        val token = generateSecureToken()
        val maskedCard = maskCardNumber(cleanCardNumber)

        // Encrypt and store in secure vault
        val encryptedCard = encryptCardData(cleanCardNumber)
        tokenVault[token] = encryptedCard

        auditLogger.logCardTokenization(token, maskedCard, merchantId)

        return CardToken(
            token = token,
            maskedCardNumber = maskedCard,
            cardType = detectCardType(cleanCardNumber),
            expiryHash = generateExpiryHash(cleanCardNumber)
        )
    }

    fun detokenizeCard(token: String, merchantId: String): String? {
        if (!isValidToken(token)) {
            auditLogger.logInvalidTokenAccess(token, merchantId)
            throw SecurityException("Invalid token format")
        }

        val encryptedCard = tokenVault[token] ?: return null
        val cardNumber = decryptCardData(encryptedCard)
        auditLogger.logCardDetokenization(token, merchantId)

        return cardNumber
    }

    private fun generateSecureToken(): String {
        val tokenBytes = ByteArray(16)
        secureRandom.nextBytes(tokenBytes)
        return "tok_${Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)}"
    }

    private fun maskCardNumber(cardNumber: String): String {
        return if (cardNumber.length >= 4) {
            "**** **** **** ${cardNumber.takeLast(4)}"
        } else "****"
    }

    private fun detectCardType(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "VISA"
            cardNumber.startsWith("5") || cardNumber.startsWith("2") -> "MASTERCARD"
            cardNumber.startsWith("3") -> "AMEX"
            cardNumber.startsWith("6") -> "DISCOVER"
            else -> "UNKNOWN"
        }
    }

    private fun validateCardNumber(cardNumber: String) {
        val cleaned = cardNumber.replace(Regex("[^0-9]"), "")
        if (cleaned.length < 13 || cleaned.length > 19) {
            throw IllegalArgumentException("Invalid card number length")
        }

        if (!isValidLuhn(cleaned)) {
            throw IllegalArgumentException("Invalid card number checksum")
        }
    }

    private fun isValidLuhn(cardNumber: String): Boolean {
        var sum = 0
        var alternate = false

        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].toString().toInt()

            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = (digit % 10) + 1
                }
            }

            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    private fun encryptCardData(cardData: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(cardData.toByteArray(StandardCharsets.UTF_8))

        val combined = iv + encryptedBytes
        return Base64.getEncoder().encodeToString(combined)
    }
    private fun decryptCardData(encryptedData: String): String {
        val combined = Base64.getDecoder().decode(encryptedData)
        val iv = combined.sliceArray(0..11)
        val encryptedBytes = combined.sliceArray(12 until combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmParameterSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun generateExpiryHash(cardNumber: String): String {
        return Base64.getEncoder().encodeToString(
            (cardNumber.takeLast(4) + "EXPIRY_SALT").toByteArray()
        ).take(8)
    }

    private fun isValidToken(token: String): Boolean {
        return token.startsWith("tok_") && token.length > 10
    }

}

data class CardToken(
    val token: String,
    val maskedCardNumber: String,
    val cardType: String,
    val expiryHash: String
)