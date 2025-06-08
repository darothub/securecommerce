package com.example.securecommerce.v1.controller

import com.example.securecommerce.dto.PaymentRequest
import com.example.securecommerce.dto.PaymentResponse
import com.example.securecommerce.v1.service.V1PaymentService
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

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "V1 - Baseline Payment Processing", description = "Original implementation with performance and security issues")
class V1PaymentController(
    private val v1PaymentService: V1PaymentService
) {

    @PostMapping("/process")
    @Operation(summary = "Process payment (V1 - Baseline)", description = "Original slow implementation")
    fun processPayment(@Valid @RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        // ISSUE: Synchronous blocking call
        val response = v1PaymentService.processPayment(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get payment status (V1)", description = "No caching - always hits database")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<PaymentResponse> {
        // ISSUE: No caching, direct database call every time
        val response = v1PaymentService.getPaymentStatus(transactionId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "Get merchant payments (V1)", description = "No pagination - loads ALL records")
    fun getMerchantPayments(@PathVariable merchantId: String): ResponseEntity<List<PaymentResponse>> {
        // ISSUE: No pagination, loads all records
        val payments = v1PaymentService.getMerchantPayments(merchantId)
        return ResponseEntity.ok(payments)
    }
}