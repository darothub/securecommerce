package com.example.securecommerce.service

import com.example.securecommerce.entity.TestUser
import com.example.securecommerce.repository.TestUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

@Service
class TestService(
    private val testUserRepository: TestUserRepository
) {
    @Async
    fun asyncMethod(): CompletableFuture<String> {
        val threadName = getCurrentThreadName()
        println("Async method executing on thread: $threadName")
        Thread.sleep(1000)
        return CompletableFuture.completedFuture("Async result from $threadName")
    }
    @Transactional
    fun transactionalMethodWithRollback(): TestUser {
        val user = TestUser(name = "Test User", email = "test@example.com")
        val savedUser = testUserRepository.save(user)

        // Force rollback by throwing exception
        throw RuntimeException("Simulated exception for rollback test")
    }
    @Transactional
    fun successfulTransactionalMethod(): TestUser {
        return testUserRepository.save(TestUser(name = "Success User", email = "success@example.com"))
    }

    fun getCurrentThreadName(): String = Thread.currentThread().name
}