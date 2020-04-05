package com.donatella.demo.bot.presenter

import com.donatella.demo.*
import com.donatella.demo.model.UserEntityService
import com.donatella.demo.model.entities.enums.Step
import com.donatella.demo.model.entities.User
import com.donatella.demo.model.entities.Veteran
import com.donatella.demo.model.repositories.VeteranRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot

@Component
class HackBotTextController(
    private val userEntityService: UserEntityService,
    private val veteranRepository: VeteranRepository,
    private val wowController: WoWController,
    private val colorizeController: ColorizeController
) {

    fun processText(
        telegramBot: TelegramLongPollingBot,
        inputMessage: String,
        chatId: String,
        firstName: String?,
        lastName: String?,
        userName: String?
    ): String {
        val userInDb = userEntityService.findOrCreateUser(chatId, firstName, lastName, userName)
        when (inputMessage) {
            START_COMMAND -> {
                if (userInDb.currentStep == Step.SIMILARITY_RIGHT_NOW) {
                    return START_TEXT
                }
                userInDb.currentStep = Step.FIRST_ENTRY
                userEntityService.save(userInDb)
                return START_TEXT
            }
            BUTTON_UPLOAD_VETERAN -> {
                if (userInDb.currentStep == Step.SIMILARITY_RIGHT_NOW) {
                    return "Подождите, мы обрабатываем ваши фото..."
                }
                return processReadyToSaveVeteran(userInDb)
            }
            BUTTON_SIMILARITY -> {
                if (!userInDb.isLoadedGrandpaPhoto) {
                    return "Сначала загрузи фото ветерана"
                }
                if (userInDb.currentStep == Step.SIMILARITY_RIGHT_NOW) {
                    return "Подождите, мы обрабатываем ваши фото..."
                }
                // На сколько похож на ветерана
                userInDb.currentStep = Step.WANT_TO_LOADING_USER_PHOTO
                userEntityService.save(userInDb)

                return "Загрузите ваше одиночное селфи, и мы покажем, на сколько похожи Вы и ваш ветеран"
            }
            BUTTON_COLORIZE -> {
                colorizeController.processColorizePhoto(telegramBot, chatId)
            }
            BUTTON_WOW -> {
                wowController.whoIsYourDad(telegramBot, chatId)
            }
            BUTTON_MENU -> {
                if (userInDb.currentStep == Step.SIMILARITY_RIGHT_NOW) {
                    return ""
                }
                userInDb.currentStep = Step.FIRST_ENTRY
                userEntityService.save(userInDb)
            }
        }

        if (userInDb.currentStep == Step.SIMILARITY_RIGHT_NOW) {
            return "Подождите, мы обрабатываем ваши фото..."
        }
        if (userInDb.currentStep == Step.WANT_TO_SAVE_VETERAN_NAME) {
            return processVeteranName(userInDb, inputMessage)
        }
        if (userInDb.currentStep == Step.WANT_TO_LOADING_USER_PHOTO) {
            return "Загрузите своё одиночное селфи или выйдите в главное меню"
        }

        return ""
    }

    // -----------------------------------------------------------------------------------------------------------------

    private fun processVeteranName(user: User, veteranNameMessage: String): String {
        user.currentStep = Step.FIRST_ENTRY
        user.isLoadedGrandpaPhoto = true
        userEntityService.save(user)

        veteranRepository.save(Veteran().also {
            it.user = user
            it.veteranName = veteranNameMessage
            it.veteranPhotoIndex = user.countOfVeteransPhoto
        })

        return VETERAN_NAME_UPLOADED
    }

    private fun processReadyToSaveVeteran(user: User): String {
        user.currentStep = Step.WANT_TO_SAVE_VETERAN_PHOTO
        userEntityService.save(user)

        return "Загрузи фотографию(портет) ветерана - желательно своего родственника"
    }

}