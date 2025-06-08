package com.example.securecommerce.service.v1

import com.example.securecommerce.domain.Payment
import com.example.securecommerce.domain.PaymentStatus
import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.exception.PaymentNotFoundException
import com.example.securecommerce.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class V1PaymentService(
    private val paymentRepository: PaymentRepository,
    private val v1PaymentGatewayService: V1PaymentGatewayService,
    private val v1FraudDetectionService: V1FraudDetectionService
) {

    fun processPayment(request: PaymentRequest): PaymentResponse {
        val transactionId = generateTransactionId()

        // SECURITY ISSUE: Store raw card data including CVV
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
        val fraudResult = v1FraudDetectionService.checkFraud(request)
        if (fraudResult.isHighRisk) {
            updatePaymentStatus(transactionId, PaymentStatus.FAILED, "Fraud detected")
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
        val gatewayResult = v1PaymentGatewayService.processPayment(request, transactionId)

        val finalStatus = if (gatewayResult.isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
        updatePaymentStatus(transactionId, finalStatus, gatewayResult.errorMessage)


        return PaymentResponse(
            transactionId = transactionId,
            status = finalStatus.name,
            message = if (gatewayResult.isSuccess) "Payment processed successfully" else "Payment failed",
            amount = request.amount,
            currency = request.currency,
        )
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

    private fun updatePaymentStatus(transactionId: String, status: PaymentStatus, failureReason: String? = null) {
        paymentRepository.findByTransactionId(transactionId)?.let { payment ->
            val updatedPayment = payment.copy(
                status = status,
                processedAt = LocalDateTime.now(),
                failureReason = failureReason
            )
            paymentRepository.save(updatedPayment)
        }
    }

    private fun generateTransactionId(): String {
        return "TXN_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}