package com.donatella.demo.bot.presenter

import com.donatella.demo.VETERAN_PHOTO_UPLOADED
import com.donatella.demo.api.SimilarityChecker
import com.donatella.demo.model.UserEntityService
import com.donatella.demo.model.entities.enums.Step
import com.donatella.demo.model.repositories.VeteranRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.Comparator
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.io.File


@Component
class HackBotPhotoController(
    private val userEntityService: UserEntityService,
    private val veteranRepository: VeteranRepository,
    private val similarityChecker: SimilarityChecker
) {

    fun processPhoto(
        telegramBot: TelegramLongPollingBot,
        userPhotos: List<PhotoSize>,
        chatId: String
    ): String? {
        val user = userEntityService.findById(chatId)
        if (user?.currentStep == Step.WANT_TO_SAVE_VETERAN_PHOTO) {
            user.countOfVeteransPhoto++
            user.currentStep = Step.WANT_TO_SAVE_VETERAN_NAME
            val saveSuccess = savePhoto(
                telegramBot,
                userPhotos,
                System.getProperty("user.dir") + "\\photos" + "\\" + chatId + "_" + user.countOfVeteransPhoto + ".jpg"
            )
            if (!saveSuccess) {
                return null
            }
            userEntityService.save(user)
            return VETERAN_PHOTO_UPLOADED
        }

        if (user?.currentStep == Step.WANT_TO_LOADING_USER_PHOTO) {
            user.countOfUserPhoto++
            user.currentStep = Step.SIMILARITY_RIGHT_NOW
            userEntityService.save(user)
            val saveSuccess = savePhoto(
                telegramBot,
                userPhotos,
                System.getProperty("user.dir") + "\\photos" + "\\users\\" + chatId + "_" + user.countOfUserPhoto + ".jpg"
            )
            if (!saveSuccess) {
                return null
            }

            val message = SendMessage()
            message.chatId = chatId
            message.text = "Принято! Обработка может занять до пары минут, как будет готово - мы пришлём результат!"
            telegramBot.execute(message)

            val similarityPercent = similarityChecker.howMuchSimilarityPercent(
                System.getProperty("user.dir") + "\\photos" + "\\users\\" + chatId + "_" + user.countOfUserPhoto + ".jpg",
                System.getProperty("user.dir") + "\\photos" + "\\" + chatId + "_" + user.countOfVeteransPhoto + ".jpg",
                chatId
            )

            user.currentStep = Step.FIRST_ENTRY
            userEntityService.save(user)
            if (similarityPercent == -1) {
                return "На изображении не найдено лицо ;("
            }

            sendImageUploadingAFile(
                telegramBot,
                System.getProperty("user.dir") + "\\photos" + "\\" + chatId + "_" + user.countOfVeteransPhoto + ".jpg",
                chatId
            )
            sendImageUploadingAFile(
                telegramBot,
                System.getProperty("user.dir") + "\\photos" + "\\users\\" + chatId + "_" + user.countOfUserPhoto + ".jpg",
                chatId
            )
            return getInfoByPercent(similarityPercent)
        }

        return ""
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

    private fun getInfoByPercent(similarityPercent: Int): String {
        val value = if (similarityPercent != 100) {
            "0.$similarityPercent"
        } else {
            "100%"
        }
        return when {
            similarityPercent < 50 -> "Схожесть: $value . Может, внешне вы и не похожи на этого человека, но внутри - вы такой же герой!"
            similarityPercent < 70 -> "Схожесть: $value . В ваших лицах есть что-то общее, но не каждый это сможет заметить"
            similarityPercent < 80 -> "Схожесть: $value . Вы похожи на этого человека! Оказавшись вместе в одно время, вас бы приняли за братьев"
            similarityPercent < 91 -> "Схожесть: $value . Невероятное сходство спустя поколения! Многие бы позавидовали вашей внешности!"
            else -> "Схожесть: $value . Вы решили нас разыграть, и отправили 2 фотографии одного человека?"
        }
    }

    private fun savePhoto(telegramBot: TelegramLongPollingBot, userPhotos: List<PhotoSize>, photoPath: String): Boolean {
        val maxSizePhoto = userPhotos.stream()
            .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null) ?: return false
        val requestFilePath = getFilePath(telegramBot, maxSizePhoto) ?: return false
        val requestPhotoFile = downloadPhotoByFilePath(telegramBot, requestFilePath) ?: return false

        val fileInProjectDir = java.io.File(photoPath)
        fileInProjectDir.createNewFile()

        copyPhotoToProjectDir(requestPhotoFile, fileInProjectDir)
        requestPhotoFile.delete()

        return true
    }

    private fun getFilePath(telegramBot: TelegramLongPollingBot, photo: PhotoSize): String? {
        Objects.requireNonNull(photo)

        if (photo.hasFilePath()) { // If the file_path is already present, we are done!
            return photo.filePath
        } else { // If not, let find it
            // We create a GetFile method and set the file_id from the photo
            val getFileMethod = GetFile()
            getFileMethod.fileId = photo.fileId
            try {
                // We execute the method using AbsSender::execute method.
                val file = telegramBot.execute(getFileMethod)
                // We now have the file_path
                return file.filePath
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun downloadPhotoByFilePath(telegramBot: TelegramLongPollingBot, filePath: String): java.io.File? {
        try {
            // Download the file calling AbsSender::downloadFile method
            return telegramBot.downloadFile(filePath)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
        return null
    }

    private fun copyPhotoToProjectDir(file1: java.io.File, file2: java.io.File) {
        val src = FileInputStream(file1).channel
        val dest = FileOutputStream(file2).channel
        dest.transferFrom(src, 0, src.size())
        src.close()
        dest.close()
    }
}