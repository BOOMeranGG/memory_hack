package com.donatella.demo.bot.presenter

import com.donatella.demo.model.UserEntityService
import com.donatella.demo.model.dto.ColorizeImageDto
import org.apache.commons.io.FileUtils
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.io.FileOutputStream
import java.util.*

private const val COLORIZE_URI = "https://8b7f58b8.ngrok.io/colorized-face"

@Component
class ColorizeController(
    private val userEntityService: UserEntityService
) {

    fun processColorizePhoto(
        telegramBot: TelegramLongPollingBot,
        chatId: String
    ) {
        val user = userEntityService.findById(chatId)
        val veteranFile = File(System.getProperty("user.dir") + "\\photos" + "\\" + chatId + "_" + user?.countOfVeteransPhoto + ".jpg")
        val veteranBytes = FileUtils.readFileToByteArray(veteranFile)

        val veteranBase64 = Base64
            .getEncoder()
            .encodeToString(veteranBytes)

        val colorizeImageDto = ColorizeImageDto().also {
            it.chatId = chatId
            it.base64String = veteranBase64
        }

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(colorizeImageDto, headers)
        val colorizedImage = restTemplate.postForObject(COLORIZE_URI, request, ColorizeImageDto::class.java)

        val message = SendMessage()
        message.chatId = chatId
        message.text = "Принято! Через несколько секунд отправлю оцифрованное фото тебе"
        telegramBot.execute(message)

        val decodedImageBytes = Base64.getDecoder().decode(colorizedImage?.base64String?.toByteArray(charset("UTF-8")))
        val fileDest = System.getProperty("user.dir") + "\\photos" + "\\colorized\\" + chatId + "_" + user?.countOfVeteransPhoto + ".jpg"

        val fos = FileOutputStream(fileDest)
        fos.write(decodedImageBytes)
        fos.close()

        sendImageUploadingAFile(telegramBot, fileDest, chatId)
    }

    // -----------------------------------------------------------------------------------------------------------------

    private fun sendImageUploadingAFile(telegramBot: TelegramLongPollingBot, filePath: String, chatId: String) {
        // Create send method
        val sendPhotoRequest = SendPhoto()
        // Set destination chat id
        sendPhotoRequest.chatId = chatId
        // Set the photo file as a new photo (You can also use InputStream with a method overload)
        sendPhotoRequest.setPhoto(File(filePath))
        //sendPhotoRequest.photo = InputFile(filePath)
        try {
            // Execute the method
            telegramBot.execute(sendPhotoRequest)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}