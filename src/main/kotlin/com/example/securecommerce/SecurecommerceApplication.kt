package com.example.securecommerce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SecurecommerceApplication

fun main(args: Array<String>) {
    runApplication<SecurecommerceApplication>(*args)
}
