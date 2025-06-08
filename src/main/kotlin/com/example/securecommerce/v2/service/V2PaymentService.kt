package com.example.securecommerce.v2.service

import com.example.securecommerce.domain.Payment
import com.example.securecommerce.domain.PaymentStatus
import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.exception.PaymentNotFoundException
import com.example.securecommerce.repository.PaymentRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.Executor

@Service
@Transactional
class V2PaymentService(
    private val paymentRepository: PaymentRepository,
    private val fraudDetectionService: V2FraudDetectionService,
    private val gatewayService: V2PaymentGatewayService,
    private val meterRegistry: MeterRegistry,
    @Qualifier("v2PaymentProcessingTimer") private val processingTimer: Timer,
    @Qualifier("v2AsyncTaskExecutor") private val asyncExecutor: Executor
) {
    suspend fun processPaymentOptimized(request: PaymentRequest): PaymentResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val transactionId = generateTransactionId()

        // STILL INSECURE: Raw card data storage, but faster processing
        val payment = Payment(
            merchantId = request.merchantId,
            amount = request.amount,
            currency = request.currency,
            transactionId = transactionId,
            cardNumber = request.cardNumber, // STILL INSECURE: Plain text
            cardholderName = request.cardholderName,
            expiryMonth = request.expiryMonth,
            expiryYear = request.expiryYear,
            cvv = request.cvv, // STILL INSECURE: Storing CVV
            status = PaymentStatus.PENDING
        )

        val savedPayment = paymentRepository.save(payment)

        // PERFORMANCE IMPROVEMENT: Parallel processing
        val fraudCheckDeferred = async { fraudDetectionService.checkFraudAsync(request) }
        val gatewayValidationDeferred = async { gatewayService.validateCardAsync(request) }

        val fraudResult = fraudCheckDeferred.await()
        val validationResult = gatewayValidationDeferred.await()

        if (fraudResult.isHighRisk) {
            updatePaymentStatus(transactionId, PaymentStatus.FAILED, "Fraud detected", startTime)
            return@withContext PaymentResponse(
                transactionId = transactionId,
                status = "FAILED",
                message = "Payment declined due to security concerns",
                amount = request.amount,
                currency = request.currency,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
        if (!validationResult.isValid) {
            updatePaymentStatus(transactionId, PaymentStatus.FAILED, validationResult.errorMessage, startTime)
            return@withContext PaymentResponse(
                transactionId = transactionId,
                status = "FAILED",
                message = "Card validation failed",
                amount = request.amount,
                currency = request.currency,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }

        // PERFORMANCE IMPROVEMENT: Optimized gateway call
        val gatewayResult = gatewayService.processPaymentAsync(request, transactionId)

        val finalStatus = if (gatewayResult.isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
        updatePaymentStatus(transactionId, finalStatus, gatewayResult.errorMessage, startTime)

        val processingTime = System.currentTimeMillis() - startTime

        PaymentResponse(
            transactionId = transactionId,
            status = finalStatus.name,
            message = if (gatewayResult.isSuccess) "Payment processed successfully" else "Payment failed",
            amount = request.amount,
            currency = request.currency,
            processingTimeMs = processingTime
        )
    }
    @Cacheable(value = ["v2PaymentStatus"], key = "#transactionId")
    fun getPaymentStatusCached(transactionId: String): PaymentResponse {
        // PERFORMANCE IMPROVEMENT: Redis caching
        val payment = paymentRepository.findByTransactionId(transactionId)
            ?: throw PaymentNotFoundException("Payment not found: $transactionId")

        return PaymentResponse(
            transactionId = payment.transactionId,
            status = payment.status.name,
            message = "Payment status retrieved (cached)",
            amount = payment.amount,
            currency = payment.currency,
            processingTimeMs = payment.processingTimeMs
        )
    }

    @Cacheable(value = ["v2MerchantPayments"], key = "#merchantId + '_' + #page + '_' + #size")
    fun getMerchantPaymentsPaginated(merchantId: String, page: Int, size: Int): List<PaymentResponse> {
        // PERFORMANCE IMPROVEMENT: Pagination + caching
        val pageable = PageRequest.of(page, size)
        return paymentRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable).map { payment ->
            PaymentResponse(
                transactionId = payment.transactionId,
                status = payment.status.name,
                message = "Payment record (paginated)",
                amount = payment.amount,
                currency = payment.currency,
                processingTimeMs = payment.processingTimeMs
            )
        }
    }

    suspend fun processBatchPayments(requests: List<PaymentRequest>): List<PaymentResponse> = withContext(Dispatchers.IO) {
        // PERFORMANCE IMPROVEMENT: Concurrent batch processing
        requests.map { request ->
            async { processPaymentOptimized(request) }
        }.awaitAll()
    }

    fun getPerformanceMetrics(): Map<String, Any> {
        val avgProcessingTime = paymentRepository.getAverageProcessingTime("MERCHANT_123") ?: 0.0

        return mapOf(
            "version" to "v2",
            "avgProcessingTimeMs" to avgProcessingTime,
            "optimizations" to listOf(
                "Async processing with coroutines",
                "Redis caching for lookups",
                "Paginated queries",
                "Parallel fraud detection and validation",
                "Optimized database connections",
                "Batch processing capabilities"
            ),
            "stillMissing" to listOf(
                "❌ Card data security (still storing raw data)",
                "❌ Authentication/authorization",
                "❌ Input sanitization",
                "❌ Rate limiting",
                "❌ Audit logging"
            )
        )
    }

    private fun updatePaymentStatus(transactionId: String, status: PaymentStatus, failureReason: String?, startTime: Long) {
        paymentRepository.findByTransactionId(transactionId)?.let { payment ->
            val updatedPayment = payment.copy(
                status = status,
                processedAt = LocalDateTime.now(),
                failureReason = failureReason,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
            paymentRepository.save(updatedPayment)
        }
    }

    private fun generateTransactionId(): String {
        return "TXN_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}