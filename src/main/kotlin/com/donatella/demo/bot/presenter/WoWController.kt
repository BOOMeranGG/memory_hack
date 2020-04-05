package com.donatella.demo.bot.presenter

import com.donatella.demo.ALLIANCE_URL
import com.donatella.demo.ORDA_URL
import com.donatella.demo.model.UserEntityService
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.util.*

@Component
class WoWController(
    private val userEntityService: UserEntityService
) {

    fun whoIsYourDad(
        telegramBot: TelegramLongPollingBot,
        chatId: String
    ) {
        val user = userEntityService.findById(chatId)
        val veteranFile = File(System.getProperty("user.dir") + "\\photos" + "\\" + chatId + "_" + user?.countOfVeteransPhoto + ".jpg")
        val veteranBytes = FileUtils.readFileToByteArray(veteranFile)
        val veteranHash = Base64
            .getEncoder()
            .encodeToString(veteranBytes)
            .hashCode()

        val isAlliance = veteranHash % 2 == 0
        if (isAlliance) {
            sendImageByUrl(telegramBot, ALLIANCE_URL, chatId)
            val message = SendMessage()
            message.chatId = chatId
            message.text = "Ваш дед ЗА АЛЬЯНС!"
            telegramBot.execute(message)
        } else {
            sendImageByUrl(telegramBot, ORDA_URL, chatId)
            val message = SendMessage()
            message.chatId = chatId
            message.text = "Ваш дед ЗА ОРДУ!"
            telegramBot.execute(message)
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private fun sendImageByUrl(telegramBot: TelegramLongPollingBot, url: String, chatId: String) {
        // Create send method
        val sendPhotoRequest = SendPhoto()
        // Set destination chat id
        sendPhotoRequest.chatId = chatId
        // Set the photo url as a simple photo
        sendPhotoRequest.setPhoto(url)
        try {
            // Execute the method
            telegramBot.execute(sendPhotoRequest)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}