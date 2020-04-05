package com.donatella.demo.model

import com.donatella.demo.model.entities.User
import com.donatella.demo.model.repositories.UserRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserEntityService(
    private val userRepository: UserRepository
) {

    fun findById(id: String): User? {
        return userRepository.findByChatId(id)
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }

    fun findOrCreateUser(id: String, firstName: String?, lastName: String?, userName: String?): User {
        val userInDb = findById(id)
        val resultUser: User?

        if (userInDb != null) {
            resultUser = userInDb
        } else {
            val newUser = User().also {
                it.chatId = id
                it.firstName = firstName
                it.lastName = lastName
                it.userName = userName
            }

            resultUser = save(newUser)
        }

        return resultUser
    }
}