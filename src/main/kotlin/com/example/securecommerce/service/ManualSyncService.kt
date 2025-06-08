package com.example.securecommerce.service

import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class ManualSyncService {

    // This method simulates what @Async would do WITHOUT @EnableAsync
    // (it would just execute synchronously)
    fun simulateAsyncWithoutEnableAsync(): CompletableFuture<String> {
        val threadName = Thread.currentThread().name
        println("Simulated 'async' method executing on thread: $threadName")
        Thread.sleep(1000) // This will block the calling thread
        return CompletableFuture.completedFuture("Sync result from $threadName")
    }
}