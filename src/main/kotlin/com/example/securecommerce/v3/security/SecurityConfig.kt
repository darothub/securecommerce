package com.example.securecommerce.v3.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.configurationSource { CorsConfiguration().applyPermitDefaultValues() } }
            .exceptionHandling { exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)}
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    // Public endpoints
                    .requestMatchers("/api/v3/auth/**").permitAll()
                    .requestMatchers("/api/v4/auth/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                    .requestMatchers("/api/v1/**", "/api/v2/**").permitAll() // V1, V2 remain open

                    // V3+ endpoints require authentication
                    .requestMatchers(HttpMethod.POST, "/api/v3/payments/**").hasRole("MERCHANT")
                    .requestMatchers(HttpMethod.GET, "/api/v3/payments/**").hasRole("MERCHANT")
                    .requestMatchers(HttpMethod.POST, "/api/v4/payments/**").hasRole("MERCHANT")
                    .requestMatchers(HttpMethod.GET, "/api/v4/payments/**").hasRole("MERCHANT")

                    .anyRequest().authenticated()
            }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}