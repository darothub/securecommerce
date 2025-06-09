package com.example.securecommerce.v3.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "V3 Authentication request")
data class V3AuthRequest(
    @field:NotBlank val merchantId: String,
    @field:NotBlank val apiKey: String
)
@Schema(description = "V3 Authentication response")
data class V3AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400,
    val merchantId: String
)