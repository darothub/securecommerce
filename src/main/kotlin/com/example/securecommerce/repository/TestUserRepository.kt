package com.example.securecommerce.repository

import com.example.securecommerce.entity.TestUser
import org.springframework.data.repository.CrudRepository

interface TestUserRepository: CrudRepository<TestUser, Long>