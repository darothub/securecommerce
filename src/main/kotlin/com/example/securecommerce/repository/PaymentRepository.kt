package com.example.securecommerce.repository

import com.example.securecommerce.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    fun findByTransactionId(transactionId: String): Payment?

    // PERFORMANCE ISSUE: No indexing strategy
    fun findByMerchantId(merchantId: String): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.merchantId = ?1 ORDER BY p.createdAt DESC")
    fun findByMerchantIdOrderByCreatedAtDesc(merchantId: String): List<Payment>
}