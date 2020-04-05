package com.donatella.demo.model.repositories

import com.donatella.demo.model.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, String> {

    fun findByChatId(id: String): User?
}