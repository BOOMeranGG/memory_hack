package com.donatella.demo.model.entities

import com.donatella.demo.model.entities.enums.Step
import javax.persistence.*

@Entity
@Table(name = "telegram_user")
class User {

    @Id
    @Column(name = "chat_id")
    var chatId: String = ""

    @Column(name = "first_name")
    var firstName: String? = null

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "user_name")
    var userName: String? = null

    @Column(name = "is_loaded_grandpa_photo")
    var isLoadedGrandpaPhoto: Boolean = false

    @Column(name = "current_step")
    @Enumerated(EnumType.STRING)
    var currentStep = Step.FIRST_ENTRY

    @Column(name = "count_of_veterans_photo")
    var countOfVeteransPhoto: Int = 0

    @Column(name = "count_of_user_photo")
    var countOfUserPhoto: Int = 0
}
