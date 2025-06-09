package com.example.securecommerce.v3.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val auditLogger: SecurityAuditLogger
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null && jwtAuthenticationService.validateToken(jwt)) {
                val merchantId = jwtAuthenticationService.getMerchantIdFromToken(jwt)
                val roles = listOf("ROLE_MERCHANT") // Simplified for demo

                val authorities = roles.map { SimpleGrantedAuthority(it) }
                val authentication = UsernamePasswordAuthenticationToken(merchantId, null, authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication

                // Add security headers
                response.setHeader("X-Content-Type-Options", "nosniff")
                response.setHeader("X-Frame-Options", "DENY")
                response.setHeader("X-XSS-Protection", "1; mode=block")
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            }
        } catch (ex: Exception) {
            auditLogger.logSecurityEvent(
                "JWT_AUTHENTICATION_ERROR",
                mapOf(
                    "error" to (ex.message ?: "Unknown error"),
                    "requestUri" to request.requestURI,
                    "action" to "AUTHENTICATION_FAILED"
                )
            )
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}