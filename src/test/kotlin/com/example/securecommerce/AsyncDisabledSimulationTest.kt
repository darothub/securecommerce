package com.example.securecommerce

import com.example.securecommerce.service.ManualSyncService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb3",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class AsyncDisabledSimulationTest {

    @Autowired
    private lateinit var manualSyncService: ManualSyncService

    @Test
    fun `simulate behavior without EnableAsync annotation`() {
        println("=== SIMULATED BEHAVIOR (WITHOUT @EnableAsync) ===")

        val mainThreadName = Thread.currentThread().name
        println("Main thread: $mainThreadName")

        // This simulates what would happen if @EnableAsync was not present
        val startTime = System.currentTimeMillis()
        val future = manualSyncService.simulateAsyncWithoutEnableAsync()
        val endTime = System.currentTimeMillis()

        val executionTime = endTime - startTime
        println("Execution time: ${executionTime}ms")

        // Without @EnableAsync, the method would block for the full sleep duration
        assertThat(executionTime).isGreaterThan(900) // Should be close to 1000ms

        val result = future.get()
        println("Result: $result")

        // Would run on the same thread
        assertThat(result).contains(mainThreadName)

        println("❌ Without @EnableAsync: Methods run synchronously on same thread")
    }
}