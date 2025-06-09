package com.example.securecommerce.v3.controller

import com.example.securecommerce.v3.dto.V3SecurePaymentRequest
import com.example.securecommerce.v3.dto.V3SecurePaymentResponse
import com.example.securecommerce.v3.service.V3SecurePaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/payments")
@Tag(name = "V3 - Security Focused", description = "✅ PCI DSS compliant with tokenization & authentication")
@SecurityRequirement(name = "bearerAuth")
class V3PaymentController(private val paymentService: V3SecurePaymentService) {

    @PostMapping("/process")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "V3 Secure Payment", description = "✅ PCI DSS compliant processing with tokenization")
    fun processSecurePayment(@Valid @RequestBody request: V3SecurePaymentRequest): ResponseEntity<V3SecurePaymentResponse> {
        val merchantId = SecurityContextHolder.getContext().authentication.name
        val response = paymentService.processSecurePayment(request, merchantId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "V3 Get Status", description = "✅ Secure authenticated access")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<V3SecurePaymentResponse> {
        val merchantId = SecurityContextHolder.getContext().authentication.name
        val response = paymentService.getPaymentStatusSecure(transactionId, merchantId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/security-metrics")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "V3 Security Metrics", description = "✅ Security compliance information")
    fun getSecurityMetrics(): ResponseEntity<Map<String, Any>> {
        val merchantId = SecurityContextHolder.getContext().authentication.name
        return ResponseEntity.ok(paymentService.getSecurityMetrics(merchantId))
    }
}