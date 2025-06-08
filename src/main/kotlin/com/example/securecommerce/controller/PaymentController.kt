package com.example.securecommerce.controller

import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Processing", description = "Credit card payment processing endpoints")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process a credit card payment")
    fun processPayment(@Valid @RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        // PERFORMANCE ISSUE: Synchronous blocking call
        val response = paymentService.processPayment(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get payment status", description = "Retrieve payment status by transaction ID")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<PaymentResponse> {
        // PERFORMANCE ISSUE: No caching, direct database call every time
        val response = paymentService.getPaymentStatus(transactionId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "Get merchant payments", description = "Retrieve all payments for a merchant")
    fun getMerchantPayments(@PathVariable merchantId: String): ResponseEntity<List<PaymentResponse>> {
        // PERFORMANCE ISSUE: No pagination, loads all records
        val payments = paymentService.getMerchantPayments(merchantId)
        return ResponseEntity.ok(payments)
    }

    // Async endpoint (we'll optimize this)
    @PostMapping("/process-async")
    @Operation(summary = "Process payment asynchronously", description = "Process payment with async handling")
    fun processPaymentAsync(@Valid @RequestBody request: PaymentRequest): CompletableFuture<ResponseEntity<PaymentResponse>> {
        return paymentService.processPaymentAsync(request)
            .thenApply { ResponseEntity.ok(it) }
    }
}