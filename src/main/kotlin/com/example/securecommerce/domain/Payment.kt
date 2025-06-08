package com.example.securecommerce.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    val merchantId: String = "",

    @Column(nullable = false)
    val amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 3)
    val currency: String = "",

    @Column(nullable = false, unique = true)
    val transactionId: String = "",

    // SECURITY ISSUE: Storing sensitive card data (we'll fix this)
    @Column(nullable = false)
    val cardNumber: String = "",

    @Column(nullable = false)
    val cardholderName: String = "",

    @Column(nullable = false)
    val expiryMonth: Int = 1,

    @Column(nullable = false)
    val expiryYear: Int = 2025,

    @Column(nullable = false)
    val cvv: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.PENDING,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val processedAt: LocalDateTime? = null,

    @Column
    val failureReason: String? = null
)
enum class PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
}