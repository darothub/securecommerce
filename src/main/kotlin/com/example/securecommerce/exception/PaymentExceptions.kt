package com.example.securecommerce.exception

class PaymentNotFoundException(message: String) : RuntimeException(message)
class PaymentProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class FraudDetectedException(message: String) : RuntimeException(message)