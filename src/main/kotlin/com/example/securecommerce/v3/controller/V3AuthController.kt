package com.example.securecommerce.v3.controller

import com.example.securecommerce.v3.dto.V3AuthRequest
import com.example.securecommerce.v3.dto.V3AuthResponse
import com.example.securecommerce.v3.security.JwtAuthenticationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/auth")
@Tag(name = "V3 Authentication", description = "✅ Secure JWT authentication")
class V3AuthController(private val jwtService: JwtAuthenticationService) {

    @PostMapping("/login")
    @Operation(summary = "V3 Merchant Login", description = "✅ Secure authentication with JWT")
    fun login(@Valid @RequestBody request: V3AuthRequest): ResponseEntity<V3AuthResponse> {
        // ✅ SECURITY: Validate credentials
        val isValid = validateMerchant(request.merchantId, request.apiKey)

        if (!isValid) {
            return ResponseEntity.status(401).build()
        }

        val token = jwtService.generateToken(request.merchantId)

        return ResponseEntity.ok(
            V3AuthResponse(
                accessToken = token,
                merchantId = request.merchantId
            )
        )
    }

    private fun validateMerchant(merchantId: String, apiKey: String): Boolean {
        // ✅ SECURITY: In production, use encrypted database
        val validMerchants = mapOf(
            "TEST_MERCHANT" to "test_api_key_98765432109876543210987654321098",
            "DEMO_MERCHANT" to "demo_api_key_11111111111111111111111111111111"
        )
        return validMerchants[merchantId] == apiKey
    }
}