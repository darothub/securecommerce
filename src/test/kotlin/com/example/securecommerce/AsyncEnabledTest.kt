package com.example.securecommerce

import com.example.securecommerce.repository.TestUserRepository
import com.example.securecommerce.service.TestService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [SecurecommerceApplication::class])
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.org.springframework.transaction=DEBUG"
    ]
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AsyncEnabledTest {
    @Autowired
    private lateinit var testService: TestService

    @Autowired
    private lateinit var userRepository: TestUserRepository

    @Test
    fun `test async method runs asynchronously with EnableAsync`(){
        val mainThreadName = Thread.currentThread().name
        println("=== ASYNC ENABLED TEST ===")
        println("Main thread name: $mainThreadName")

        // Test async method
        val startTime = System.currentTimeMillis()
        val future = testService.asyncMethod()
        val immediateTime = System.currentTimeMillis()

        // Should return immediately (CompletableFuture returned without waiting)
        val immediateExecutionTime = immediateTime - startTime
        println("Time to get CompletableFuture: ${immediateExecutionTime}ms")
        assertThat(immediateExecutionTime).isLessThan(100) // Should be immediate

        // Now wait for the actual result
        val result = future.get(3, TimeUnit.SECONDS)
        val totalTime = System.currentTimeMillis() - startTime
        println("Async result: $result")
        println("Total time including wait: ${totalTime}ms")

        // Verify it ran on a different thread (Spring's task executor)
        assertThat(result).contains("task-") // Spring's default async thread pool
        assertThat(result).doesNotContain(mainThreadName)

        println("✅ Async method ran on different thread: SUCCESS")
    }
    @Test
    fun `test transaction rollback works with auto-configuration`() {
        userRepository.deleteAll()

        println("=== TRANSACTION ROLLBACK TEST ===")
        println("Initial user count: ${userRepository.count()}")

        // This should rollback due to exception
        assertThrows<RuntimeException> {
            testService.transactionalMethodWithRollback()
        }

        // Verify rollback occurred
        val countAfterRollback = userRepository.count()
        println("User count after rollback: $countAfterRollback")
        assertThat(countAfterRollback).isEqualTo(0)

        println("✅ Transaction rollback works: SUCCESS")
    }

    @Test
    fun `test successful transaction commit`() {
        userRepository.deleteAll()

        println("=== SUCCESSFUL TRANSACTION TEST ===")
        val savedUser = testService.successfulTransactionalMethod()

        assertThat(savedUser.id).isGreaterThan(0)
        assertThat(userRepository.count()).isEqualTo(1)

        println("✅ Successful transaction works: SUCCESS")
    }
}