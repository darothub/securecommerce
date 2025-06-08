package com.example.securecommerce.v2.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class V2PerformanceConfig {
    @Bean("v2PaymentProcessingTimer")
    fun v2PaymentProcessingTimer(meterRegistry: MeterRegistry): Timer {
        return Timer.builder("v2.payment.processing.time")
            .description("V2 Payment processing time")
            .register(meterRegistry)
    }

    @Bean("v2AsyncTaskExecutor")
    fun v2AsyncTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 50
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("v2-async-payment-")
        executor.initialize()
        return executor
    }
}