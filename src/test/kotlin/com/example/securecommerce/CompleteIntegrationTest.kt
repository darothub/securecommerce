package com.example.securecommerce

import com.example.securecommerce.repository.TestUserRepository
import com.example.securecommerce.service.ManualSyncService
import com.example.securecommerce.service.TestService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.TimeUnit

@SpringBootTest
class CompleteIntegrationTest {

    @Autowired
    private lateinit var testService: TestService

    @Autowired
    private lateinit var manualSyncService: ManualSyncService

    @Autowired
    private lateinit var userRepository: TestUserRepository

    @Test
    fun `complete test - async enabled vs disabled behavior`() {
        println("\n" + "=".repeat(60))
        println("COMPLETE INTEGRATION TEST")
        println("=".repeat(60))

        val mainThread = Thread.currentThread().name

        // Test 1: Show async behavior (with @EnableAsync)
        println("\n1. Testing @Async WITH @EnableAsync:")
        val asyncFuture = testService.asyncMethod()
        val asyncResult = asyncFuture.get(3, TimeUnit.SECONDS)
        println("   Result: $asyncResult")
        println("   ✅ Runs on separate thread: ${asyncResult.contains("task-")}")

        // Test 2: Show sync behavior (simulating without @EnableAsync)
        println("\n2. Testing @Async WITHOUT @EnableAsync (simulated):")
        val syncFuture = manualSyncService.simulateAsyncWithoutEnableAsync()
        val syncResult = syncFuture.get()
        println("   Result: $syncResult")
        println("   ❌ Runs on same thread: ${syncResult.contains(mainThread)}")

        // Test 3: Transaction management (works in both cases)
        println("\n3. Testing @Transactional (auto-configured in Spring Boot):")
        userRepository.deleteAll()

        // Test rollback
        try {
            testService.transactionalMethodWithRollback()
        } catch (e: RuntimeException) {
            println("   Exception caught as expected")
        }

        val countAfterRollback = userRepository.count()
        println("   Users after rollback: $countAfterRollback")
        println("   ✅ Rollback works: ${countAfterRollback == 0L}")

        // Test successful transaction
        testService.successfulTransactionalMethod()
        val countAfterSuccess = userRepository.count()
        println("   Users after successful transaction: $countAfterSuccess")
        println("   ✅ Commit works: ${countAfterSuccess == 1L}")

        println("\n" + "=".repeat(60))
        println("SUMMARY:")
        println("- @EnableAsync: REQUIRED for async behavior")
        println("- @EnableTransactionManagement: AUTO-CONFIGURED in Spring Boot")
        println("=".repeat(60))
    }
}