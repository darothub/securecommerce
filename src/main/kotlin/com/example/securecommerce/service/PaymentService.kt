package com.example.securecommerce.service

import com.example.securecommerce.domain.Payment
import com.example.securecommerce.domain.PaymentStatus
import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.exception.PaymentNotFoundException
import com.example.securecommerce.repository.PaymentRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGatewayService: PaymentGatewayService,
    private val fraudDetectionService: FraudDetectionService
) {

    fun processPayment(request: PaymentRequest): PaymentResponse {
        // SECURITY ISSUE: No input sanitization or encryption
        // PERFORMANCE ISSUE: Synchronous processing, no optimization

        val transactionId = generateTransactionId()

        // PERFORMANCE ISSUE: Multiple database calls without batching
        val payment = Payment(
            merchantId = request.merchantId,
            amount = request.amount,
            currency = request.currency,
            transactionId = transactionId,
            cardNumber = request.cardNumber, // SECURITY ISSUE: Plain text storage
            cardholderName = request.cardholderName,
            expiryMonth = request.expiryMonth,
            expiryYear = request.expiryYear,
            cvv = request.cvv, // SECURITY ISSUE: Storing CVV
            status = PaymentStatus.PENDING
        )

        val savedPayment = paymentRepository.save(payment)

        // PERFORMANCE ISSUE: Synchronous fraud detection (slow)
        val fraudResult = fraudDetectionService.checkFraud(request)
        if (fraudResult.isHighRisk) {
            savedPayment.copy(
                status = PaymentStatus.FAILED,
                failureReason = "Fraud detected",
                processedAt = LocalDateTime.now()
            ).let { paymentRepository.save(it) }

            return PaymentResponse(
                transactionId = transactionId,
                status = "FAILED",
                message = "Payment declined due to security concerns",
                amount = request.amount,
                currency = request.currency
            )
        }

        // PERFORMANCE ISSUE: Synchronous gateway call
        Thread.sleep(2000) // Simulate slow payment gateway

        val gatewayResult = paymentGatewayService.processPayment(request, transactionId)

        val finalStatus = if (gatewayResult.isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
        savedPayment.copy(
            status = finalStatus,
            processedAt = LocalDateTime.now(),
            failureReason = if (!gatewayResult.isSuccess) gatewayResult.errorMessage else null
        ).let { paymentRepository.save(it) }

        return PaymentResponse(
            transactionId = transactionId,
            status = finalStatus.name,
            message = if (gatewayResult.isSuccess) "Payment processed successfully" else "Payment failed",
            amount = request.amount,
            currency = request.currency
        )
    }

    @Async
    fun processPaymentAsync(request: PaymentRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            processPayment(request)
        }
    }

    fun getPaymentStatus(transactionId: String): PaymentResponse {
        // PERFORMANCE ISSUE: No caching, always hits database
        val payment = paymentRepository.findByTransactionId(transactionId)
            ?: throw PaymentNotFoundException("Payment not found: $transactionId")

        return PaymentResponse(
            transactionId = payment.transactionId,
            status = payment.status.name,
            message = "Payment status retrieved",
            amount = payment.amount,
            currency = payment.currency
        )
    }

    fun getMerchantPayments(merchantId: String): List<PaymentResponse> {
        // PERFORMANCE ISSUE: No pagination, loads all records
        return paymentRepository.findByMerchantId(merchantId).map { payment ->
            PaymentResponse(
                transactionId = payment.transactionId,
                status = payment.status.name,
                message = "Payment record",
                amount = payment.amount,
                currency = payment.currency
            )
        }
    }

    private fun generateTransactionId(): String {
        return "TXN_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}