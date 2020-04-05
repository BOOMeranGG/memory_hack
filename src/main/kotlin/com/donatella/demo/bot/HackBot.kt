package com.donatella.demo.bot

import com.donatella.demo.*
import com.donatella.demo.bot.presenter.HackBotPhotoController
import com.donatella.demo.bot.presenter.HackBotTextController
import com.donatella.demo.model.UserEntityService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.ArrayList
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup

@Component
class HackBot(
    private val hackBotPhotoController: HackBotPhotoController,
    private val hackBotTextController: HackBotTextController,
    private val userEntityService: UserEntityService
) : TelegramLongPollingBot() {


    @Value("\${telegram.bot.token}")
    private val telegramBotToken: String = ""

    override fun getBotUsername(): String {
        return "WoWMemoryBot"
    }

    override fun getBotToken(): String {
        return telegramBotToken
    }

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) {
            return
        }

        val userMessage = update.message
        val sendMessage = sendCustomKeyboard(update.message.chatId.toString())
        Thread {
            if (userMessage.hasText()) {
                controlTextMessage(userMessage, sendMessage)
            } else if (userMessage.hasPhoto()) {
                controlPhotoMessage(userMessage, sendMessage)
            }
        }.start()
    }

    // -----------------------------------------------------------------------------------------------------------------

    fun controlPhotoMessage(inputMessage: Message, sendMessage: SendMessage) {
        var savePhotoResult = hackBotPhotoController.processPhoto(
            this,
            inputMessage.photo,
            inputMessage.chatId.toString()
        )
        if (savePhotoResult == null) {
            println("Почему-то savePhotoResult == null")
            savePhotoResult = "Почему-то savePhotoResult == null"
        }
        if (savePhotoResult.isEmpty()) {
            return
        }

        sendMessage.text = savePhotoResult
        try {
            execute(sendMessage)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    fun controlTextMessage(inputMessage: Message, sendMessage: SendMessage) {
        val textResult = hackBotTextController.processText(
            this,
            inputMessage.text,
            inputMessage.chatId.toString(),
            inputMessage.from.firstName,
            inputMessage.from.lastName,
            inputMessage.from.userName
        )

        if (textResult.isEmpty()) {
            return
        }
        sendMessage.text = textResult
        try {
            execute(sendMessage)
            //executeAsync(sendMessage, MyAsync(this, inputMessage.chatId.toString()))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun sendCustomKeyboard(chatId: String): SendMessage {
        val message = SendMessage()
        message.chatId = chatId
        message.text = "MemoryBot"

        // Create ReplyKeyboardMarkup object
        val keyboardMarkup = ReplyKeyboardMarkup()
        // Create the keyboard (list of keyboard rows)
        val keyboard = ArrayList<KeyboardRow>()
        // Create a keyboard row
        val keyboards = getKeyboardsForUser(chatId)
        keyboards.forEach { keyboardName ->
            val row = KeyboardRow()
            row.add(keyboardName)
            keyboard.add(row)
        }

        // Set the keyboard to the markup
        keyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = keyboardMarkup

        return message
    }

    private fun getKeyboardsForUser(chatId: String): List<String> {
        // val user = userEntityService.findOrCreateUser(chatId)
        return listOf(BUTTON_UPLOAD_VETERAN, BUTTON_COLORIZE, BUTTON_SIMILARITY, BUTTON_WOW, BUTTON_MENU)
    }
}