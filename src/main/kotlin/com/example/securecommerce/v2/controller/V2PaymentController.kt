package com.example.securecommerce.v2.controller

import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.v2.service.V2PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/payments")
@Tag(name = "V2 - Performance Optimized", description = "High-performance payment processing with caching and async")
class V2PaymentController(
    private val paymentService: V2PaymentService
) {
    @PostMapping("/process")
    @Operation(summary = "Process payment (V2 - Optimized)", description = "Async processing with performance improvements")
    fun processPayment(@Valid @RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        val response = runBlocking {
            paymentService.processPaymentOptimized(request)
        }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get payment status (V2 - Cached)", description = "Redis cached payment status lookup")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.getPaymentStatusCached(transactionId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "Get merchant payments (V2 - Paginated)", description = "Paginated merchant payment lookup")
    fun getMerchantPayments(
        @PathVariable merchantId: String,
        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PaymentResponse>> {
        val payments = paymentService.getMerchantPaymentsPaginated(merchantId, page, size)
        return ResponseEntity.ok(payments)
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch process payments (V2)", description = "Process multiple payments concurrently")
    fun processBatchPayments(@Valid @RequestBody requests: List<PaymentRequest>): ResponseEntity<List<PaymentResponse>> {
        val responses = runBlocking {
            paymentService.processBatchPayments(requests)
        }
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/performance-metrics")
    @Operation(summary = "V2 Performance metrics", description = "Real-time performance statistics")
    fun getPerformanceMetrics(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(paymentService.getPerformanceMetrics())
    }
}