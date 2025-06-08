package com.example.securecommerce.repository

import com.example.securecommerce.domain.Payment
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    fun findByTransactionId(transactionId: String): Payment?

    // PERFORMANCE ISSUE: No indexing strategy
    fun findByMerchantId(merchantId: String): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.merchantId = ?1 ORDER BY p.createdAt DESC")
    fun findByMerchantIdOrderByCreatedAtDesc(merchantId: String): List<Payment>

    // v2+: Optimized queries with pagination
    fun findByMerchantIdOrderByCreatedAtDesc(merchantId: String, pageable: Pageable): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    fun findPaymentsByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Payment>

    // v2+: Performance analytics
    @Query("SELECT AVG(p.processingTimeMs) FROM Payment p WHERE p.merchantId = :merchantId AND p.processingTimeMs IS NOT NULL")
    fun getAverageProcessingTime(@Param("merchantId") merchantId: String): Double?

//    // v3+: Security analytics
//    @Query("SELECT AVG(p.riskScore) FROM Payment p WHERE p.merchantId = :merchantId AND p.riskScore IS NOT NULL")
//    fun getAverageRiskScore(@Param("merchantId") merchantId: String): Double?
}