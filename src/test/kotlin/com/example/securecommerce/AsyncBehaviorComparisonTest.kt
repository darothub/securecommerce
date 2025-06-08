package com.example.securecommerce

import com.example.securecommerce.service.TestService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit

@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb2",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AsyncBehaviorComparisonTest {

    @Autowired
    private lateinit var testService: TestService

    @Test
    fun `demonstrate current async behavior with EnableAsync present`() {
        println("=== CURRENT BEHAVIOR (WITH @EnableAsync) ===")

        val mainThreadName = Thread.currentThread().name
        println("Main thread: $mainThreadName")

        // Test regular method (should be same thread)
        val regularThreadName = testService.getCurrentThreadName()
        println("Regular method thread: $regularThreadName")
        assertThat(regularThreadName).isEqualTo(mainThreadName)

        // Test async method (should be different thread)
        val startTime = System.currentTimeMillis()
        val future = testService.asyncMethod()
        val quickReturnTime = System.currentTimeMillis() - startTime

        println("Time to return CompletableFuture: ${quickReturnTime}ms")
        assertThat(quickReturnTime).isLessThan(50) // Should return almost immediately

        // Get the actual result
        val result = future.get(3, TimeUnit.SECONDS)
        println("Async method result: $result")

        // Verify async execution
        assertThat(result).contains("task-")
        assertThat(result).doesNotContain(mainThreadName)

        println("✅ With @EnableAsync: Async methods run on separate threads")
    }
}